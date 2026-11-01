import React from 'react'
import { Download, Github, Shield, Cpu, HardDrive, CheckCircle } from 'lucide-react'

export const Hero: React.FC = () => {
  return (
    <section className="relative pt-14 pb-12 sm:pt-20 sm:pb-16 text-center px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-6">
        
        {/* Official App Icon Emblem Showcase */}
        <div className="flex items-center justify-center mb-3">
          <div className="relative group">
            <div className="absolute -inset-2 rounded-3xl bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] opacity-40 blur-xl group-hover:opacity-70 transition duration-500" />
            <div className="relative w-24 h-24 rounded-[28px] overflow-hidden shadow-2xl transition transform group-hover:scale-105">
              <img 
                src="/app-icon.svg" 
                alt="Reel Library App Icon" 
                className="w-full h-full object-cover" 
              />
            </div>
          </div>
        </div>

        {/* Pill Badge */}
        <div className="inline-flex items-center gap-2 rounded-full border border-pink-500/25 bg-pink-500/[0.08] px-3 sm:px-3.5 py-1 text-[11px] sm:text-xs text-pink-300 shadow-[0_0_20px_rgba(225,48,108,0.25)] max-w-full">
          <span className="w-1.5 h-1.5 rounded-full bg-pink-400 animate-pulse shrink-0"></span>
          <span className="font-medium tracking-wide truncate sm:whitespace-normal">
            Private • On-Device Speech • Multi-LLM • Offline SQLite Vault
          </span>
        </div>

        {/* Hero Title */}
        <h1 className="text-3xl sm:text-5xl lg:text-7xl font-extrabold tracking-tight font-display leading-[1.12] sm:leading-[1.08] text-white">
          Turn Fleeting Reels into a <br className="hidden sm:inline" />
          <span className="gradient-accent">Searchable Personal Vault</span>
        </h1>

        {/* Hero Subtitle */}
        <p className="max-w-2xl mx-auto text-sm sm:text-lg text-neutral-400 font-normal leading-relaxed px-2 sm:px-0">
          Save cooking recipes, coding tutorials, and travel tips from Instagram. 
          Transcribe spoken audio locally with Whisper, generate structured AI bullet summaries, 
          and store everything securely in your private offline vault.
        </p>

        {/* Primary CTA Buttons */}
        <div className="pt-3 flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4 w-full">
          <a 
            href="/release/ReelLibrary-release-v1.2.3.apk" 
            download="ReelLibrary-release-v1.2.3.apk"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 sm:gap-2.5 rounded-xl bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] px-5 sm:px-7 py-3 sm:py-3.5 text-sm font-bold text-white shadow-glow-sunset transition hover:brightness-110 active:scale-98"
          >
            <Download className="w-4 h-4 shrink-0" />
            <span>Direct APK Download</span>
            <span className="text-[11px] font-semibold tracking-tight px-2 py-0.5 rounded-md bg-white/20 ml-1">v1.2.3 • 5.5 MB</span>
          </a>

          <a 
            href="https://github.com/pratham1659/reel-library" 
            target="_blank" 
            rel="noopener noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-white/[0.12] bg-white/[0.04] px-5 sm:px-6 py-3 sm:py-3.5 text-sm font-medium text-neutral-300 transition hover:bg-white/[0.08] hover:text-white"
          >
            <Github className="w-4 h-4 shrink-0" />
            <span>View Source on GitHub</span>
          </a>
        </div>

        {/* Trust Badges Bar */}
        <div className="pt-6 flex flex-wrap items-center justify-center gap-x-4 sm:gap-x-8 gap-y-2.5 text-[11px] sm:text-xs text-neutral-400">
          <div className="flex items-center gap-1.5">
            <Cpu className="w-4 h-4 text-pink-400 shrink-0" />
            <span>Offline STT Transcription</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-pink-400 shrink-0" />
            <span>Zero Third-Party Trackers</span>
          </div>
          <div className="flex items-center gap-1.5">
            <HardDrive className="w-4 h-4 text-pink-400 shrink-0" />
            <span>Portable Universal Export</span>
          </div>
          <div className="flex items-center gap-1.5">
            <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>MIT Open Source</span>
          </div>
        </div>

      </div>
    </section>
  )
}
