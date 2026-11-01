export interface ReleaseNote {
  version: string
  date: string
  isLatest: boolean
  title: string
  summary: string
  categories: {
    category: string
    badgeColor: string
    items: {
      title: string
      description: string
    }[]
  }[]
}

export const releaseNotesData: ReleaseNote[] = [
  {
    version: 'v1.2.3',
    date: 'November 2026',
    isLatest: true,
    title: 'Data & Storage UI Polish & Groq Qwen Lock',
    summary: 'Pixel-perfect typography and alignment refinements across Data & Storage settings, alongside a strict model lock for Groq APIs.',
    categories: [
      {
        category: 'UI & Layout Polish',
        badgeColor: 'bg-pink-500/10 text-pink-300 border-pink-500/30',
        items: [
          {
            title: 'Data & Storage Alignment Polish',
            description: 'Refined typography and spacing across Data & Storage settings to prevent orphaned text lines. Universal Export subtitle optimized into a clean single line.'
          },
          {
            title: 'System Update Dual-Version Display',
            description: 'System Update dialog now displays both Current Version and Latest Version simultaneously across header badges and download status cards.'
          },
          {
            title: 'In-App Update Installation',
            description: 'Optimized package installer intent flags and enhanced user feedback for unknown source permissions and Google Play Protect verification.'
          }
        ]
      },
      {
        category: 'AI Engine Stability',
        badgeColor: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
        items: [
          {
            title: 'Groq Qwen Model Lock',
            description: 'Enforced dedicated qwen/qwen3.8-27b model usage across Groq inference pipelines without unintended model fallbacks.'
          }
        ]
      }
    ]
  },
  {
    version: 'v1.2.2',
    date: 'November 2026',
    isLatest: false,
    title: 'Offline Vault & Real-Time Telemetry',
    summary: 'A major milestone introducing live storage telemetry, direct in-app GitHub updates, and an updated brand identity.',
    categories: [
      {
        category: 'Vault & Storage',
        badgeColor: 'bg-pink-500/10 text-pink-300 border-pink-500/30',
        items: [
          {
            title: 'Universal Vault Export & Import',
            description: 'Export all transcripts, summaries, and audio archives directly into portable .zip files. Zero intermediary servers.'
          },
          {
            title: 'Real-Time Storage Breakdown',
            description: 'Accurate, live telemetry calculating real-time byte counts across audio files, transcripts, and SQLite database storage, replacing legacy mock estimates.'
          }
        ]
      },
      {
        category: 'Software Updates',
        badgeColor: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
        items: [
          {
            title: 'Direct In-App GitHub Updates',
            description: 'Switched the software update engine directly to GitHub Releases API. Obsolete Cloudflare Workers have been completely removed.'
          },
          {
            title: 'Daily Auto-Check with Safe Confirmation',
            description: 'The app checks for updates daily at startup, but never updates automatically without your explicit confirmation. You can review full release notes before confirming.'
          }
        ]
      },
      {
        category: 'Brand & UX',
        badgeColor: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
        items: [
          {
            title: 'Vibrant Sunset Gradient & Modernized Identity',
            description: 'Original high-contrast sunset pink and purple gradient matching the official app icon, with the camera flash dot removed for clean branding.'
          },
          {
            title: 'Local On-Device LLM (Coming Soon)',
            description: 'Offline neural summarization via Gemma/Ollama is marked as Coming Soon in active development, complementing the live BYOK cloud engine.'
          }
        ]
      }
    ]
  }
]
