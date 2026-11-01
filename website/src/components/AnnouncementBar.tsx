import React from 'react'
import { ArrowRight, Sparkles } from 'lucide-react'

export const AnnouncementBar: React.FC = () => {
  return (
    <header className="relative z-50">
      <a 
        href="#release-notes" 
        className="group block border-b border-white/[0.06] bg-obsidian/80 backdrop-blur-md px-4 py-2 text-center text-xs text-neutral-400 transition hover:bg-white/[0.02]"
      >
        <div className="flex items-center justify-center gap-2 sm:gap-3 text-center">
          <span className="inline-flex items-center gap-1 rounded-full bg-pink-500/10 px-2 py-0.5 text-[10px] sm:text-xs font-semibold text-pink-400 border border-pink-500/30 whitespace-nowrap shrink-0">
            <Sparkles className="w-2.5 h-2.5" />
            v1.2.3 Released
          </span>
          <span className="hidden sm:inline text-neutral-300 font-medium truncate max-w-md">
            New: Storage Telemetry, Qwen LLM & In-App Updates
          </span>
          <span className="hidden sm:inline text-neutral-600">•</span>
          <span className="text-pink-400 font-semibold group-hover:translate-x-0.5 transition-transform inline-flex items-center gap-1 text-[11px] sm:text-xs whitespace-nowrap shrink-0">
            Read Release Notes <ArrowRight className="w-3 h-3" />
          </span>
        </div>
      </a>
    </header>
  )
}
