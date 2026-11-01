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
    version: 'v1.2.4',
    date: 'November 2026',
    isLatest: true,
    title: 'Privacy Policy Onboarding, Audio Handling, Native Files Export & 16 KB Alignment',
    summary: 'Introduces an onboarding Privacy Policy gate, intelligent handling for copyright-gated audio, native "Save to Files" vault backup export, 16 KB ELF page-alignment for Android 15, and dedicated Knowledge Drawer filters.',
    categories: [
      {
        category: 'Privacy & Security',
        badgeColor: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
        items: [
          {
            title: 'Privacy Policy Onboarding Screen',
            description: 'New users are welcomed with an explicit Privacy Policy gate affirming Zero Phone Permissions, Local Encrypted Vault storage, Bring Your Own Keys (BYOK), and Zero Telemetry.'
          },
          {
            title: 'Full Policy & Terms Dialog',
            description: 'A detailed 7-section Privacy Policy & Terms modal explaining client-side storage, encrypted credentials, and BYOK direct-to-provider routing.'
          }
        ]
      },
      {
        category: 'Vault Export & Knowledge Drawer',
        badgeColor: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
        items: [
          {
            title: 'Native "Save to Files" Export',
            description: 'Integrated Android Storage Access Framework (CreateDocument), allowing one-tap direct saving of .zip backup archives into any local folder (Downloads, Documents, Drive, or SD Card).'
          },
          {
            title: 'Audio Not Available Drawer Filter',
            description: 'New sidebar filter chip with live counters specifically for reels with missing or copyright-blocked audio, while keeping caption summaries fully functional.'
          },
          {
            title: 'Instant Full Vault Count',
            description: 'Knowledge Drawer loads all reels directly from the Room database on first open, decoupled from main feed pagination.'
          }
        ]
      },
      {
        category: 'Media Extraction & Transcripts',
        badgeColor: 'bg-pink-500/10 text-pink-300 border-pink-500/30',
        items: [
          {
            title: 'Intelligent Audio & Copyright Detection',
            description: 'Detects reels where Instagram restricts media download or copyright-blocks licensed audio, extracting song and artist metadata and conserving AI quota.'
          },
          {
            title: 'Descriptive Transcript Explanations',
            description: 'Replaced generic labels with helpful, context-rich reasons and post caption references in Full Transcript, Reader Mode, and Reel Detail views.'
          },
          {
            title: 'Single-Line Whisper Status Container',
            description: 'Redesigned audio transcription loading indicator into a clean, single-line neumorphic status pill with progress spinner.'
          }
        ]
      },
      {
        category: 'Platform & Performance',
        badgeColor: 'bg-blue-500/10 text-blue-300 border-blue-500/30',
        items: [
          {
            title: '16 KB ELF Alignment (Android 15)',
            description: 'Upgraded MediaPipe GenAI engine to 0.10.24 with 16384-byte ELF segment alignment and uncompressed page packaging for Android 15 compatibility.'
          },
          {
            title: 'Multi-Status Distinct Color Palette',
            description: 'Assigned unique, high-contrast color palettes for every status (Ready, Audio Not Available, Queued, Extracting, Transcribing, Summarizing, Failed) across dark and light themes.'
          }
        ]
      },
      {
        category: 'AI Knowledge Synthesis',
        badgeColor: 'bg-purple-500/10 text-purple-300 border-purple-500/30',
        items: [
          {
            title: 'Dynamic & Detailed Actionable Takeaways',
            description: 'AI prompts no longer artificially force 5 points on brief reels and now extract every practical takeaway available on content-rich reels in full detail with exact tools, steps, and syntax.'
          },
          {
            title: 'Expanded Generation Tokens',
            description: 'Increased maximum token budget to 2,048 tokens across Groq, OpenAI, Claude, and local models to ensure extensive takeaways are never truncated.'
          }
        ]
      }
    ]
  },
  {
    version: 'v1.2.3',
    date: 'November 2026',
    isLatest: false,
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
