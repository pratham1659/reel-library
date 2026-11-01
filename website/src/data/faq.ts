export interface FaqItem {
  question: string
  answer: string
}

export const faqList: FaqItem[] = [
  {
    question: "How does Reel Library download and transcribe Instagram Reels?",
    answer: "When you share a reel link from Instagram to Reel Library, the app extracts the audio stream using on-device processing. It then passes the audio to Whisper locally to produce an accurate, timestamped transcript without routing your credentials through third-party servers."
  },
  {
    question: "Is Local On-Device LLM available now, or is it coming soon?",
    answer: "Whisper speech-to-text and SQLite Room indexing are 100% on-device and operational today. Local on-device LLM summarization (running lightweight neural models like Gemma directly on your phone) is currently in active development and marked as Coming Soon for the v1.3 release. Today, summarization uses secure BYOK cloud APIs (Gemini, Groq, OpenAI)."
  },
  {
    question: "How does the new Google Drive Personal Cloud feature work?",
    answer: "Starting in v1.2.2, you can link your private Google Drive via Google Sign-In. The app communicates directly with the official Google Drive API using a secure, scoped access token (drive.file). Your summaries, audio notes, and backups are stored directly in your own cloud account—no middleman or intermediate backend has access to your files."
  },
  {
    question: "Is Reel Library free, or is there a hidden monthly subscription?",
    answer: "Reel Library is 100% free and open-source under the MIT license. There are no monthly fees, no ads, and no premium paywalls. You can use free-tier API keys from Google AI Studio (Gemini) or Groq, or run STT fully offline."
  },
  {
    question: "How do in-app software updates work?",
    answer: "Reel Library queries the GitHub Releases API directly for new official APK releases once per day. When an update is detected, an explicit confirmation dialog presents the full markdown release notes, allowing you to review changes before tapping to download and install the new APK."
  },
  {
    question: "Which cloud AI models are supported under BYOK?",
    answer: "Reel Library features a BYOK (Bring-Your-Own-Key) multi-engine architecture supporting Google Gemini (Gemini 1.5 Flash / Pro), Groq (Llama 3 70B / 8B), OpenAI (GPT-4o, GPT-3.5), Anthropic Claude, and DeepSeek."
  }
]
