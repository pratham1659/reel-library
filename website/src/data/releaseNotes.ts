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
            title: 'Data & Storage Alignment Refinements',
            description: 'Balanced Google Drive card header with single-line status pills, aligned Room SQLite Database metrics with secondary reel count sublabels, and perfected drive.file scope security icon alignment.'
          },
          {
            title: 'Storage Breakdown Typography',
            description: 'Clean top-aligned baselines for database sizes and item counts, ensuring crisp readability across diverse screen densities.'
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
    title: 'Personal Cloud Vault & Real-Time Telemetry',
    summary: 'A major milestone introducing private Google Drive personal cloud backups, live storage telemetry, direct in-app GitHub updates, and an updated brand identity.',
    categories: [
      {
        category: 'Personal Cloud',
        badgeColor: 'bg-pink-500/10 text-pink-300 border-pink-500/30',
        items: [
          {
            title: 'Google Drive Scoped Backup',
            description: 'Link your personal Google Account via Google Sign-In to sync transcripts, summaries, and audio directly to your own Google Drive (drive.file scope). Zero intermediary servers.'
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
