# Reel Library 📱⚡

An intelligent, on-device & cloud-accelerated Instagram Reel bookmarking and research vault for Android. Captures reels directly from the Instagram Share sheet, extracts spoken audio transcripts, synthesizes executive takeaways, and tracks real-time cloud telemetry without any demo or mock data.

---

## 🌟 Key Highlights

- **Dual-Engine Architecture**:
  - **Cloud-Fast Mode**: Sub-second speech transcription via Groq Cloud Whisper (`whisper-large-v3-turbo`) and executive summarization via Google Gemini (`gemini-3.5-flash-lite`) and Groq (`qwen/qwen3.8-27b`).
  - **Local On-Device Mode**: 100% offline, privacy-first processing leveraging device CPU/NPU (Gemma 2B + Whisper.cpp).
  - **Auto-Hybrid Fallback**: Dynamically routes requests based on real-time network availability.
- **Real-Time Cloud Telemetry & Live Token Tracking**:
  - Live network ping measurement displaying exact **latency in milliseconds** (e.g. `⚡ 165ms`).
  - Color-coded **HTTP status codes** (`200 OK`, `401 Unauthorized`, `429 Rate Limited`).
  - **Zero Demo Data**: Live token telemetry tracked directly from API responses (`prompt_tokens`, `completion_tokens`, `total_tokens`).
- **Deep Content Extraction**:
  - Direct Instagram CDN MP4 audio scraping.
  - Spoken voiceover transcription with Groq Whisper.
  - Fallback to rich post captions when spoken audio is absent.
  - Granular executive takeaways and AI-generated topic tags.
- **Modern Neumorphic Design**:
  - Obsidian Dark & Obsidian Light aesthetic tailored for high-contrast readability.
  - Collapsible audio transcript & post caption cards supporting rich emoji and one-tap copy.
  - In-reel "Retry AI" reprocessing with live status badges.
  - Fluid, horizontal scroll tags on grid cards.

---

## 🛠️ Tech Stack

- **Platform**: Android 16 (API 36) / Minimum API 26
- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose with Material 3 & Custom Neumorphic Design System
- **Database**: Room Database with FTS4 Full-Text Search
- **Networking**: OkHttp 4.12
- **Serialization**: Kotlinx Serialization JSON
- **Preferences**: Jetpack DataStore (Preferences)
- **Background Work**: AndroidX WorkManager

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17
- Android SDK 36 (Platform 36)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/pratham1659/reel-library.git
   cd reel-library
   ```

2. **Configure API Keys** (Optional, can be entered inside the app):
   - Launch Reel Library on your device.
   - Open **Settings** ➔ **Select Models**.
   - Input your free API keys for:
     - **Groq API Key**: [Groq Cloud Console](https://console.groq.com)
     - **Google Gemini API Key**: [Google AI Studio](https://aistudio.google.com)
   - Tap **Test Cloud** to verify connectivity, live latency, and token consumption.

3. **Build and Run**:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔒 Security & Privacy

- All API keys are saved locally in private Android DataStore encrypted storage.
- No sensitive credentials, keys, or keystores are committed to version control.
- Live token tracking reads strictly from active network responses without external telemetry tracking.

---

## 📄 License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.
