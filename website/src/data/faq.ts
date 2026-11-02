export interface FaqItem {
  question: string
  answer: string
}

export const faqList: FaqItem[] = [
  {
    question: "How does Reel Library download and transcribe Instagram Reels?",
    answer: "When you share a reel link from Instagram to Reel Library, the app extracts the audio stream using on-device processing. It then passes the audio to Whisper locally or via your configured Groq/OpenAI cloud provider to produce an accurate, timestamped transcript without routing your credentials through third-party middleman servers."
  },
  {
    question: "Why does Android show a Play Protect or 'Unrecognized app' warning during install?",
    answer: "Because Reel Library is distributed independently outside the Google Play Store on GitHub, Android treats our developer signing certificate as new. This is standard behavior for all sideloaded open-source APKs. To proceed: tap 'More details' on the prompt, then tap 'Install anyway'. Reel Library contains zero trackers, zero ads, and minimal permissions."
  },
  {
    question: "How do I verify the SHA-256 checksum of the downloaded APK?",
    answer: "Run `sha256sum ReelLibrary-release-v1.2.5.apk` in your terminal. Compare the output to our official published hash: 17225c02bff6f11841734accf55ae5c6941a6c5c6878b43389cce6a71f2420c7. Every GitHub Release also includes an attached SHA256SUMS.txt file for automated verification (`sha256sum -c SHA256SUMS.txt`)."
  },
  {
    question: "What Android permissions does Reel Library declare, and why?",
    answer: "Reel Library requests strictly scoped permissions: INTERNET (for audio extraction and direct BYOK AI provider calls), ACCESS_NETWORK_STATE (to monitor Wi-Fi connectivity for the Auto-Hybrid dispatcher), and POST_NOTIFICATIONS (to alert you when background transcription finishes). High-risk permissions like REQUEST_INSTALL_PACKAGES, QUERY_ALL_PACKAGES, SYSTEM_ALERT_WINDOW, and BIND_ACCESSIBILITY_SERVICE are completely removed."
  },
  {
    question: "Where is my data stored and how can I export it?",
    answer: "All your transcripts, extracted audio notes, and AI summaries are stored in an encrypted offline SQLite database on your device. You can export your entire library at any time into a universal .zip archive containing standard JSON and media files."
  },
  {
    question: "Is Local On-Device LLM available now, or is it coming soon?",
    answer: "Whisper speech-to-text and SQLite Room indexing are 100% on-device and operational today. Local on-device LLM summarization (running lightweight neural models like Gemma directly on your phone) is currently in active development for the v1.3 release. Today, summarization uses secure BYOK cloud APIs (Groq, Gemini, OpenAI, Claude)."
  },
  {
    question: "Is Reel Library free, or is there a hidden monthly subscription?",
    answer: "Reel Library is 100% free and open-source under the MIT license. There are no monthly fees, no ads, and no premium paywalls. You can use free-tier API keys from Groq Console or Google AI Studio, or run STT fully offline."
  },
  {
    question: "How do in-app software updates work?",
    answer: "Reel Library checks the official GitHub Releases API once daily for new updates. When a new version is available, a non-intrusive notification lets you view the release notes and safely redirects you to GitHub Releases in your default browser to download the verified APK without needing dangerous package installer permissions."
  },
  {
    question: "Which cloud AI models are supported under BYOK?",
    answer: "Reel Library features a BYOK (Bring-Your-Own-Key) multi-engine architecture supporting Groq (Llama 3.3 70B, Qwen 2.5, Whisper Turbo), Google Gemini (Gemini 1.5 Flash / Pro), OpenAI (GPT-4o, Whisper-1), Anthropic Claude (Claude 3.5 Sonnet / Haiku), and custom OpenAI-compatible endpoints (DeepSeek, Ollama, Together AI)."
  }
]
