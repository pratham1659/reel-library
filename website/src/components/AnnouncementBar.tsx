import React from 'react'
import { ArrowRight, Sparkles } from 'lucide-react'

export const AnnouncementBar: React.FC = () => {
  return (
    <header className="relative z-50">
      <a 
        href="#release-notes" 
        className="group block border-b border-white/[0.06] bg-obsidian/80 backdrop-blur-md px-4 py-2 text-center text-xs text-neutral-400 transition hover:bg-white/[0.02]"
      >
        <div className="inline-flex items-center gap-2 flex-wrap justify-center">
          <span className="inline-flex items-center gap-1 rounded-full bg-cyan-500/10 px-2 py-0.5 text-[10px] font-semibold text-cyan-400 border border-cyan-500/30">
            <Sparkles className="w-2.5 h-2.5" />
            v1.2.2 Released
          </span>
          <span className="text-neutral-300 font-medium">
            New: Google Drive Personal Cloud, Real-time Storage & In-App Updates
          </span>
          <span className="text-cyan-400 font-semibold group-hover:translate-x-1 transition-transform inline-flex items-center gap-0.5">
            Read Release Notes <ArrowRight className="w-3 h-3" />
          </span>
        </div>
      </a>
    </header>
  )
}
