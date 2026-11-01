export interface FaqItem {
  question: string
  answer: string
}

export const faqList: FaqItem[] = [
  {
    question: "How does Reel Library download and transcribe Instagram Reels?",
    answer: "When you share a reel link from Instagram to Reel Library, the app extracts the audio stream using on-device processing. It then passes the audio to Whisper or your selected STT engine to produce an accurate, timestamped transcript without routing your credentials through third-party servers."
  },
  {
    question: "Is Reel Library free, or is there a hidden monthly subscription?",
    answer: "Reel Library is 100% free and open-source under the MIT license. There are no monthly fees, no ads, and no premium paywalls. You can use free-tier API keys from Google AI Studio (Gemini) or Groq, or run fully offline."
  },
  {
    question: "How does the new Google Drive Personal Cloud feature work?",
    answer: "Starting in v1.2.2, you can link your private Google Drive via Google Sign-In. The app communicates directly with the official Google Drive API using a secure, scoped access token. Your summaries, audio notes, and backups are stored directly in your own cloud account—no middleman or intermediate backend has access to your files."
  },
  {
    question: "Can I use Reel Library offline?",
    answer: "Yes! All saved reels, transcripts, tags, and notes are indexed locally in an on-device SQLite Room database. You can search, browse, copy, and export your entire reel vault completely offline."
  },
  {
    question: "Which AI models and providers are supported?",
    answer: "Reel Library features a BYOK (Bring-Your-Own-Key) multi-engine architecture supporting Google Gemini (e.g. Gemini 1.5 Flash / Pro), Groq (Llama 3 70B / 8B), OpenAI (GPT-4o, GPT-3.5), Anthropic Claude, and local self-hosted endpoints like Ollama."
  },
  {
    question: "How do in-app updates work?",
    answer: "Reel Library queries the GitHub Releases API directly for new official APK releases. When an update is detected, you can inspect the full markdown release notes and choose to download and install the new version with one tap."
  }
]
