export interface ComparisonRow {
  feature: string
  reelLibrary: string | boolean
  instagramSaved: string | boolean
  genericNotes: string | boolean
}

export const comparisonData: ComparisonRow[] = [
  {
    feature: "Automatic Speech-to-Text Transcription",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: false
  },
  {
    feature: "AI Bullet Summaries & Ingredient Extraction",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: "Manual entry only"
  },
  {
    feature: "Full-Text Search Across Spoken Audio",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: false
  },
  {
    feature: "Universal Portable Vault Export (.zip)",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: "Requires premium tier"
  },
  {
    feature: "Direct One-Tap Share Sheet Ingestion",
    reelLibrary: true,
    instagramSaved: "In-app only",
    genericNotes: "Clunky manual paste"
  },
  {
    feature: "Zero Trackers, Ads, or Subscriptions",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: false
  },
  {
    feature: "Bring Your Own Key (BYOK) Flexibility",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: false
  },
  {
    feature: "100% Open-Source Codebase (MIT)",
    reelLibrary: true,
    instagramSaved: false,
    genericNotes: false
  }
]
