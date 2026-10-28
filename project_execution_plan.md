# Project Plan: ReelRecall (Dual-Engine: Cloud & On-Device Instagram Reel Knowledge Vault)

## 1. Project Overview & Objectives
**ReelRecall** is an Android application designed to capture Instagram Reels shared directly from the Instagram app via Android's native share sheet (`ACTION_SEND`). It extracts the post caption, audio, and metadata, transcribes the spoken content, generates structured actionable summaries and tags, indexes them locally in a searchable Room FTS5 vault, and enables one-tap deep linking back to the Reel in the Instagram app.

### Core Architectural Principles
- **Dual-Engine Architecture (User’s Choice):** Full support for both:
  1. **Cloud Free-Tier Mode:** Groq Cloud (`whisper-large-v3`) + Google AI Studio (`gemini-1.5-flash`) for sub-second, zero-cost processing.
  2. **100% Local / On-Device Mode:** Local `whisper.cpp` (whisper-base/tiny) + On-device MediaPipe LLM Inference (Gemma 2-2B / Qwen 2.5-1.5B) for complete offline privacy and independence.
  3. **Auto / Hybrid Fallback:** Automatically uses Cloud for blazing speed when online, falling back to Local when offline or if API limits are hit.
- **Hardware-Accelerated Local Inference:** Tailored to utilize high-memory flagship hardware (e.g., 12GB RAM, multi-core NPU/GPU) with zero risk of Out-Of-Memory crashes.
- **Zero-Bloat Media Pipeline:** Eliminates heavy external FFmpeg binaries by leveraging Android's native `MediaExtractor` / `MediaMuxer` and Groq's direct container audio support.
- **Idempotent & Resilient Ingestion:** Strips Instagram tracking query parameters (`?igsh=...`), normalizes canonical shortcodes, and guarantees single-card indexing.
- **Privacy-First Local Vault:** Transcripts, summaries, and tags remain permanently indexed on-device in a Room SQLite database with full-text search (FTS5).

---

## 2. Technical Architecture & Dual-Engine Ingestion Flow

```
[ Instagram App ]
       │
       │ Share Sheet (ACTION_SEND with Reel URL + Tracking Params)
       ▼
[ ShareReceiverActivity (Android) ]
       │
       ├─► 1. Normalize URL (strip ?igsh=, extract canonical shortcode)
       ├─► 2. Upsert DB placeholder (State: QUEUED)
       ├─► 3. Enqueue WorkManager Job (ReelIngestWorker)
       └─► 4. finish() immediately (< 50ms) to return user to Instagram
                │
                ▼
[ Ingestion Engine (WorkManager) ]
       │
       ├─► Step A: Metadata & Media Extraction
       │     • Primary: Client-side headless HTTP parser (extract og:video / sharedData JSON)
       │     • Fallback: Free Cloudflare Worker proxy / open endpoint
       │
       ├─► Step B: Audio Stream Handling
       │     • Demux audio track to .m4a via Android native MediaExtractor / MediaMuxer (0MB APK overhead)
       │
       ├─► Step C: Transcription Dispatcher (Strategy Pattern)
       │     ┌─────────────────────────────────────────────────────────────┐
       │     │ Check User Preference & Connectivity:                       │
       │     │  • If CLOUD / ONLINE: Groq Whisper API (whisper-large-v3)    │
       │     │  • If LOCAL / OFFLINE: whisper.cpp (whisper-tiny / base)     │
       │     └─────────────────────────────────────────────────────────────┘
       │
       ├─► Step D: Summarization Dispatcher (Strategy Pattern)
       │     ┌─────────────────────────────────────────────────────────────┐
       │     │ Check User Preference & Connectivity:                       │
       │     │  • If CLOUD / ONLINE: Gemini 1.5 Flash (Structured Schema)  │
       │     │  • If LOCAL / OFFLINE: MediaPipe LLM (Gemma 2B / Qwen 1.5B) │
       │     └─────────────────────────────────────────────────────────────┘
       │
       ▼
[ Room Database + FTS5 Index ]
       │
       ├─► Transition State: QUEUED ➔ TRANSCRIBING ➔ SUMMARIZING ➔ COMPLETED
       └─► SQLite FTS5 table synchronizes automatically for instant local keyword search
                │
                ▼
[ Jetpack Compose UI ]
       │
       ├─► Live Feed with Status Badges (Real-time updates via Room Flow)
       ├─► Instant FTS5 Search & Categorized Tag Chips
       ├─► One-Tap Deep Link to Instagram (`com.instagram.android`)
       └─► Settings / Engine Preference (Cloud vs Local vs Auto + API Key & Model Downloader)
```

---

## 3. Technology Stack & Dual-Engine Matrix

| Layer | Option A: Cloud Free-Tier | Option B: 100% On-Device / Local | Purpose |
| :--- | :--- | :--- | :--- |
| **Transcription Engine** | **Groq Cloud API** (`whisper-large-v3`)<br>• Speed: ~0.8s<br>• Cost: $0 (20 RPM free)<br>• Size: 0 MB | **whisper.cpp / Whisper-JNI**<br>• Speed: ~10-25s<br>• Cost: $0<br>• Size: ~75MB–140MB (tiny/base) | Audio-to-text conversion |
| **Summarization Engine** | **Gemini 1.5 Flash API**<br>• Speed: ~1.2s<br>• Cost: $0 (1,500 req/day free)<br>• Guaranteed JSON Schema | **Google MediaPipe GenAI**<br>• Model: Gemma-2-2B-it / Qwen2.5-1.5B<br>• Speed: ~10-20 tok/sec on NPU/GPU<br>• Size: ~1.3GB–1.8GB | Extraction of key takeaways, titles, and tags |
| **Media Handling** | Android native `MediaExtractor` / `MediaMuxer` (Direct stream handling, zero FFmpeg bloat) | Same native Android API | Demuxing audio from video container |
| **Persistence** | Room SQLite Database + **SQLite FTS5** | Same Room + FTS5 | Sub-millisecond keyword search across transcripts & summaries |
| **Background Orchestration** | Android Jetpack `WorkManager` (Persistent background queue with network constraints) | Same WorkManager | Guarantees jobs finish even if the user switches apps |
| **Client UI** | Kotlin + Jetpack Compose + Material 3 | Same Jetpack Compose UI | Fluid UI with live state cards |

---

## 4. Architectural Highlights & Strategy Interfaces

### 1. Dual-Engine Abstraction (`Strategy Pattern`)
The ingest pipeline remains unified and decoupled through clean interfaces:

```kotlin
interface TranscriptionEngine {
    suspend fun transcribe(audioFile: File): Result<String>
}

interface SummarizationEngine {
    suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary>
}

enum class EngineMode {
    CLOUD_ONLY,  // Groq + Gemini
    LOCAL_ONLY,  // whisper.cpp + MediaPipe LLM
    AUTO_HYBRID  // Cloud when connected, Local fallback when offline/throttled
}
```

### 2. On-Device Model Manager (Settings UI)
- Users can manage local model files directly from the app's Settings screen:
  - Download `whisper-base.bin` (~140MB) or `whisper-tiny.bin` (~75MB).
  - Download `gemma-2-2b-it-gpu-int4.bin` (~1.4GB) or `qwen2.5-1.5b-instruct.bin` (~1.1GB).
  - Shows download progress, checksum verification, and local storage utilization.
  - With 12GB RAM and 256GB storage, the device will easily cache and execute both models simultaneously with zero thermal throttling or memory pressure.

### 3. Guaranteed JSON Schema (Cloud & Local)
- **Cloud (Gemini):** Configured with `response_mime_type: "application/json"` and `response_schema`.
- **Local (MediaPipe / LLM):** Enforced via structured JSON prompt framing and a regex fallback parser to sanitize JSON codeblocks (` ```json ... ``` `).

---

## 5. Phase-by-Phase Implementation Plan

### Phase 1: Foundation, Share Interception & URL Normalization
- [ ] **Step 1.1: Project Baseline & Gradle Config**
  - Initialize Gradle project with Compose Material 3, Hilt, Coroutines, Navigation Compose, and Room.
  - Setup Version Catalog (`libs.versions.toml`).
  - Add required permissions: `INTERNET`, `POST_NOTIFICATIONS`, `ACCESS_NETWORK_STATE`.
- [ ] **Step 1.2: Share Sheet Handler (`ShareReceiverActivity`)**
  - Register `ShareReceiverActivity` in `AndroidManifest.xml` targeting `ACTION_SEND` (`text/plain`).
  - Implement `InstagramUrlParser` extracting canonical shortcode (`/reel/[A-Za-z0-9_-]+/`).
  - Save pending entity in Room DB with status `QUEUED`.
  - Enqueue `ReelIngestWorker` via `WorkManager`.
  - Immediately call `finish()` to keep Instagram experience uninterrupted.

### Phase 2: Media Ingestion & Native Audio Extraction
- [ ] **Step 2.1: Instagram Extractor**
  - Extract post caption, creator handle, and direct media URL:
    - Primary: Client-side HTTP request extracting OpenGraph meta tags (`og:video`, `og:description`).
    - Fallback: Lightweight proxy endpoint if Instagram returns a login wall.
- [ ] **Step 2.2: Native Audio Demuxer**
  - Use Android native `MediaExtractor` and `MediaMuxer` to strip audio track to `.m4a` in app cache directory. Zero native FFmpeg dependencies.

### Phase 3: Dual-Engine AI Layer (Cloud & Local)
- [ ] **Step 3.1: Cloud Engine Implementation**
  - `GroqWhisperEngine`: Multipart Retrofit call to Groq Whisper API (`whisper-large-v3`).
  - `GeminiSummarizerEngine`: Google AI Studio REST client with `gemini-1.5-flash` and strict JSON Schema.
- [ ] **Step 3.2: Local On-Device Engine Implementation**
  - `LocalWhisperEngine`: Integrated via JNI / whisper.cpp native library supporting `whisper-tiny.bin` and `whisper-base.bin`.
  - `LocalMediaPipeSummarizerEngine`: Integrated using `com.google.mediapipe:tasks-genai` running quantized GGUF/MediaPipe models (Gemma 2-2B / Qwen 2.5-1.5B) on GPU/NPU.
- [ ] **Step 3.3: Engine Dispatcher & Auto-Fallback**
  - `DualEngineDispatcher`: Dynamically selects engine based on `EngineMode` preference and real-time network connectivity.

### Phase 4: Room Database, FTS5 & Background Orchestrator
- [ ] **Step 4.1: Room Database Schema & FTS5**
  - `ReelEntity` with status states: `QUEUED`, `EXTRACTING`, `TRANSCRIBING`, `SUMMARIZING`, `COMPLETED`, `FAILED`.
  - `ReelFtsEntity` virtual table for instant sub-millisecond keyword searches across titles, summaries, transcripts, and tags.
- [ ] **Step 4.2: WorkManager Pipeline (`ReelIngestWorker`)**
  - Serial execution with real-time Room state updates so UI displays live progress badges.
  - Success/Failure notifications via `NotificationManager`.

### Phase 5: Modern Jetpack Compose UI & Engine Settings
- [ ] **Step 5.1: Vault Feed & Search View**
  - Search bar wired to Room FTS5 with real-time debounced queries.
  - Tag filter chips (`#productivity`, `#tech`, `#fitness`, etc.).
  - Card view showing title, summary, expandable bulleted takeaways, tags, and Instagram deep link CTA.
- [ ] **Step 5.2: Settings & Engine Preferences Screen**
  - Engine Mode Selector: **Cloud Free-Tier** vs. **100% On-Device** vs. **Auto-Hybrid**.
  - Cloud Section: Input & validate free Groq and Gemini API keys (stored in encrypted storage).
  - Local Section: Model manager showing download buttons, download progress bars, model sizes, and NPU/GPU acceleration status.

---

## 6. Immediate Next Steps to Begin Scaffolding

1. **Scaffold Gradle Project**: Setup the multi-module or clean single-module project structure with Kotlin 2.0 and Jetpack Compose.
2. **Build Data Layer & Ingestion Interfaces**: Create `ReelEntity`, Room DB, `TranscriptionEngine`, and `SummarizationEngine`.
3. **Build UI with Mock Data**: Implement the Vault feed, search bar, and Settings screen so everything is visually verifiable immediately.