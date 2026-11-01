import React from 'react'
import { Download, Github, ShieldCheck, Smartphone, Sparkles } from 'lucide-react'

export const DownloadCta: React.FC = () => {
  return (
    <section id="download" className="relative py-14 sm:py-20 px-3 sm:px-6 lg:px-8 max-w-5xl mx-auto text-center overflow-hidden w-full">
      <div className="relative rounded-3xl p-5 sm:p-10 md:p-14 bg-gradient-to-b from-obsidian-card via-obsidian-elevated to-obsidian border border-pink-500/30 shadow-[0_0_80px_rgba(225,48,108,0.25)] overflow-hidden">
        
        {/* Top Glow Ambient */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-40 bg-pink-500/20 blur-3xl pointer-events-none" />

        <div className="relative z-10 max-w-2xl mx-auto space-y-6">
          <div className="inline-flex items-center gap-1.5 rounded-full border border-pink-500/30 bg-pink-500/10 px-3 py-1 text-xs text-pink-300">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Ready for Android 8.0+ (API 26+)</span>
          </div>

          <h2 className="text-2xl sm:text-4xl md:text-5xl font-extrabold tracking-tight text-white font-display">
            Start Building Your <br className="hidden sm:inline" />
            <span className="gradient-accent">Personal Reel Knowledge Base</span>
          </h2>

          <p className="text-sm sm:text-base text-neutral-400 px-2 sm:px-0">
            Free forever, zero tracking, open-source under the MIT License. 
            Download the official APK or review the code on GitHub.
          </p>

          <div className="pt-2 flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4 w-full">
            <a
              href="/release/ReelLibrary-release-v1.2.4.apk"
              download="ReelLibrary-release-v1.2.4.apk"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 sm:gap-2.5 rounded-xl bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] px-6 sm:px-8 py-3.5 sm:py-4 text-sm font-bold text-white shadow-glow-sunset hover:brightness-110 active:scale-98 transition"
            >
              <Download className="w-4 h-4 shrink-0" />
              <span>Direct APK Download</span>
              <span className="text-[11px] font-semibold tracking-tight px-2 py-0.5 rounded-md bg-white/20 ml-1">v1.2.4 • 5.5 MB</span>
            </a>

            <a
              href="https://github.com/pratham1659/reel-library/releases"
              target="_blank"
              rel="noopener noreferrer"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-white/[0.12] bg-white/[0.04] px-6 py-4 text-sm font-medium text-neutral-300 hover:bg-white/[0.08] hover:text-white transition"
            >
              <Github className="w-4 h-4" />
              <span>View Releases on GitHub</span>
            </a>
          </div>

          <div className="pt-6 flex flex-wrap items-center justify-center gap-6 text-xs text-neutral-400">
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-pink-400" />
              Signed with Official Release Key
            </span>
            <span className="flex items-center gap-1.5">
              <Smartphone className="w-4 h-4 text-pink-400" />
              Automatic In-App Updates via GitHub
            </span>
          </div>

        </div>

      </div>
    </section>
  )
}
