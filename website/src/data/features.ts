export interface FeatureItem {
  id: string
  title: string
  tagline: string
  description: string
  badge: string
  gradient: string
  iconName: string
  highlights: string[]
}

export const bentoFeatures: FeatureItem[] = [
  {
    id: 'transcription',
    title: 'Offline Speech-to-Text',
    tagline: 'Whisper On-Device STT Engine',
    description: 'Converts reel audio into high-accuracy, timestamped text transcripts locally on your smartphone. Zero audio bytes ever leave your device without your consent.',
    badge: '100% Private',
    gradient: 'from-[#FCAF45]/15 via-[#FF5252]/10 to-transparent',
    iconName: 'Mic',
    highlights: ['Multi-language support', 'Accurate timestamps', 'Sub-second local inference']
  },
  {
    id: 'ai-summary',
    title: 'Instant Actionable Summaries',
    tagline: 'Structured Insights in Seconds',
    description: 'Transform 60-second cooking recipes, coding tutorials, and travel itineraries into structured bullet points, key takeaways, and action items.',
    badge: 'Multi-LLM',
    gradient: 'from-[#E1306C]/15 via-[#C13584]/10 to-transparent',
    iconName: 'Sparkles',
    highlights: ['Key takeaways & steps', 'Recipe ingredient extraction', 'One-tap copy & export']
  },
  {
    id: 'semantic-search',
    title: 'Full-Text & Tag Search',
    tagline: 'Find Any Saved Idea Instantly',
    description: 'Never lose a reel again. Search across spoken words, auto-generated tags, creators, and notes with millisecond SQLite Room indexing.',
    badge: 'Lightning Fast',
    gradient: 'from-[#833AB4]/15 via-[#E1306C]/10 to-transparent',
    iconName: 'Search',
    highlights: ['Deep transcript querying', 'Smart auto-categorization', 'Offline SQLite database']
  },
  {
    id: 'google-drive',
    title: 'Personal Google Drive Cloud',
    tagline: 'Your Storage, Your Encryption',
    description: 'Seamlessly backup transcripts, metadata, and media directly to your private Google Drive using scoped OAuth2. Zero middleman servers.',
    badge: 'New in v1.2.2',
    gradient: 'from-[#FF5252]/15 via-[#FCAF45]/10 to-transparent',
    iconName: 'Cloud',
    highlights: ['Direct Google Drive API', 'Personal cloud ownership', 'Real-time sync telemetry']
  },
  {
    id: 'byok',
    title: 'Bring Your Own API Keys (BYOK)',
    tagline: 'Gemini, Groq, OpenAI & Ollama',
    description: 'Plug in your own free or paid API keys. Run on Google Gemini Flash, Groq ultra-fast Llama-3, OpenAI GPT-4o, or self-hosted local LLMs.',
    badge: 'Zero Markup',
    gradient: 'from-[#C13584]/15 via-[#833AB4]/10 to-transparent',
    iconName: 'Key',
    highlights: ['Encrypted Keystore storage', 'Multiple provider support', 'No recurring subscription']
  },
  {
    id: 'open-source',
    title: 'Auditable & Open Source',
    tagline: 'Free Forever Under MIT License',
    description: 'Built with Jetpack Compose and modern Kotlin coroutines. 100% transparent code audited by the community. No ads, trackers, or dark patterns.',
    badge: 'MIT Licensed',
    gradient: 'from-[#E1306C]/15 via-[#FCAF45]/10 to-transparent',
    iconName: 'ShieldCheck',
    highlights: ['Zero telemetry trackers', 'Reproducible GitHub builds', 'In-app update engine']
  }
]
