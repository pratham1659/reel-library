import React from 'react'
import { releaseNotesData } from '../data/releaseNotes'
import { Sparkles, CheckCircle2, Cloud, RefreshCw, Cpu } from 'lucide-react'

export const ReleaseNotes: React.FC = () => {
  return (
    <section id="release-notes" className="relative py-16 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
      <div className="text-center max-w-2xl mx-auto mb-12">
        <div className="inline-flex items-center gap-1.5 rounded-full border border-pink-500/30 bg-pink-500/10 px-3 py-1 text-xs text-pink-300 mb-3">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Changelog & Updates</span>
        </div>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white font-display">
          What's New in Reel Library
        </h2>
        <p className="text-sm sm:text-base text-neutral-400 mt-2">
          Read full release highlights for recent updates and features added to the platform.
        </p>
      </div>

      <div className="space-y-8">
        {releaseNotesData.map((rel) => (
          <div
            key={rel.version}
            className="rounded-3xl p-6 sm:p-8 bg-obsidian-card border border-white/[0.08] shadow-neumorphic-md space-y-6 relative overflow-hidden"
          >
            {/* Top accent bar */}
            <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB]" />

            {/* Version Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-white/[0.06] pb-5">
              <div>
                <div className="flex items-center gap-2.5">
                  <span className="font-display text-2xl font-black text-white">{rel.version}</span>
                  {rel.isLatest && (
                    <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-pink-500/15 text-pink-300 border border-pink-500/30">
                      Latest Release
                    </span>
                  )}
                </div>
                <div className="text-sm font-semibold text-neutral-300 mt-1">{rel.title}</div>
              </div>
              <div className="text-xs font-mono text-neutral-500">{rel.date}</div>
            </div>

            <p className="text-sm text-neutral-300 leading-relaxed">
              {rel.summary}
            </p>

            {/* Feature Categories Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-2">
              {rel.categories.map((cat, idx) => (
                <div 
                  key={idx} 
                  className="rounded-2xl p-4 bg-obsidian-elevated/80 border border-white/[0.06] space-y-3 flex flex-col justify-between"
                >
                  <div className="space-y-3">
                    <span className={`inline-block text-[10px] font-mono font-semibold px-2.5 py-0.5 rounded-full border ${cat.badgeColor}`}>
                      {cat.category}
                    </span>

                    <div className="space-y-3">
                      {cat.items.map((it, itemIdx) => (
                        <div key={itemIdx} className="space-y-1">
                          <div className="text-xs font-bold text-white flex items-center gap-1.5">
                            <CheckCircle2 className="w-3.5 h-3.5 text-pink-400 shrink-0" />
                            <span>{it.title}</span>
                          </div>
                          <p className="text-[11px] text-neutral-400 leading-relaxed pl-5">
                            {it.description}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Release Footer / GitHub link */}
            <div className="pt-3 border-t border-white/[0.06] flex flex-wrap items-center justify-between gap-3 text-xs text-neutral-400">
              <div className="flex items-center gap-3">
                <span className="flex items-center gap-1.5 text-neutral-300">
                  <Cloud className="w-3.5 h-3.5 text-pink-400" /> Google Drive Scoped
                </span>
                <span className="flex items-center gap-1.5 text-neutral-300">
                  <RefreshCw className="w-3.5 h-3.5 text-emerald-400" /> Direct GitHub Updates
                </span>
                <span className="flex items-center gap-1.5 text-purple-300 font-medium">
                  <Cpu className="w-3.5 h-3.5 text-purple-400" /> Local LLM (Coming Soon)
                </span>
              </div>

              <a
                href={`https://github.com/pratham1659/reel-library/releases/tag/${rel.version}`}
                target="_blank"
                rel="noopener noreferrer"
                className="text-pink-400 hover:text-pink-300 font-medium flex items-center gap-1"
              >
                <span>View Full Markdown Changelog on GitHub &rarr;</span>
              </a>
            </div>

          </div>
        ))}
      </div>
    </section>
  )
}
