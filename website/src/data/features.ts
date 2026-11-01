export interface FeatureItem {
  id: string
  title: string
  tagline: string
  description: string
  badge: string
  badgeColor?: string
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
    badgeColor: 'bg-pink-500/10 text-pink-300 border-pink-500/30',
    gradient: 'from-[#FCAF45]/15 via-[#FF5252]/10 to-transparent',
    iconName: 'Mic',
    highlights: ['Multi-language support', 'Accurate timestamps', 'Sub-second local inference']
  },
  {
    id: 'ai-summary',
    title: 'Instant Actionable Summaries',
    tagline: 'Structured Insights in Seconds',
    description: 'Transform 60-second cooking recipes, coding tutorials, and travel itineraries into structured bullet points, key takeaways, and action items.',
    badge: 'Multi-LLM BYOK',
    badgeColor: 'bg-rose-500/10 text-rose-300 border-rose-500/30',
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
    badgeColor: 'bg-purple-500/10 text-purple-300 border-purple-500/30',
    gradient: 'from-[#5851DB]/15 via-[#E1306C]/10 to-transparent',
    iconName: 'Search',
    highlights: ['Deep transcript querying', 'Smart auto-categorization', 'Offline SQLite database']
  },
  {
    id: 'google-drive',
    title: 'Personal Google Drive Cloud',
    tagline: 'Your Storage, Your Encryption',
    description: 'Seamlessly backup transcripts, metadata, and media directly to your private Google Drive using scoped OAuth2. Zero middleman servers.',
    badge: 'New in v1.2.2',
    badgeColor: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
    gradient: 'from-[#FF5252]/15 via-[#FCAF45]/10 to-transparent',
    iconName: 'Cloud',
    highlights: ['Direct Google Drive API', 'Personal cloud ownership', 'Real-time sync telemetry']
  },
  {
    id: 'byok',
    title: 'Bring Your Own API Keys (BYOK)',
    tagline: 'Gemini, Groq, OpenAI & DeepSeek',
    description: 'Plug in your own free or paid API keys stored in Android Keystore. Zero SaaS markup and no recurring monthly subscriptions.',
    badge: 'Zero Markup',
    badgeColor: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    gradient: 'from-[#C13584]/15 via-[#5851DB]/10 to-transparent',
    iconName: 'Key',
    highlights: ['Encrypted Keystore storage', 'Multiple provider support', 'Free tier API compatible']
  },
  {
    id: 'local-llm',
    title: 'Local On-Device LLM',
    tagline: 'Gemma & Ollama Offline Inference',
    description: 'Offline neural summarization running completely on your device silicon without internet connection or external API keys.',
    badge: 'Coming Soon',
    badgeColor: 'bg-purple-500/15 text-purple-300 border-purple-500/30 animate-pulse',
    gradient: 'from-[#5851DB]/15 via-[#E1306C]/10 to-transparent',
    iconName: 'Cpu',
    highlights: ['100% on-device neural processing', 'Zero network requests', 'In active development for v1.3']
  }
]
