import React from 'react'
import { Download, Github, Shield, Cpu, Cloud, CheckCircle } from 'lucide-react'

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
        <div className="inline-flex items-center gap-2 rounded-full border border-pink-500/25 bg-pink-500/[0.08] px-3.5 py-1 text-xs text-pink-300 shadow-[0_0_20px_rgba(225,48,108,0.25)]">
          <span className="w-1.5 h-1.5 rounded-full bg-pink-400 animate-pulse"></span>
          <span className="font-medium tracking-wide">
            Private • On-Device Speech • Multi-LLM • Google Drive Backup
          </span>
        </div>

        {/* Hero Title */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight font-display leading-[1.08] text-white">
          Turn Fleeting Reels into a <br />
          <span className="gradient-accent">Searchable Personal Vault</span>
        </h1>

        {/* Hero Subtitle */}
        <p className="max-w-2xl mx-auto text-base sm:text-lg text-neutral-400 font-normal leading-relaxed">
          Save cooking recipes, coding tutorials, and travel tips from Instagram. 
          Transcribe spoken audio locally with Whisper, generate structured AI bullet summaries, 
          and sync securely to your own private Google Drive.
        </p>

        {/* Primary CTA Buttons */}
        <div className="pt-3 flex flex-col sm:flex-row items-center justify-center gap-4">
          <a 
            href="/release/ReelLibrary-release-v1.2.2.apk" 
            download="ReelLibrary-release-v1.2.2.apk"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 rounded-xl bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] px-7 py-3.5 text-sm font-bold text-white shadow-glow-sunset transition hover:brightness-110 active:scale-98"
          >
            <Download className="w-4 h-4" />
            <span>Direct APK Download</span>
            <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-white/20 ml-1">v1.2.2 • 5.7 MB</span>
          </a>

          <a 
            href="https://github.com/pratham1659/reel-library" 
            target="_blank" 
            rel="noopener noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-white/[0.12] bg-white/[0.04] px-6 py-3.5 text-sm font-medium text-neutral-300 transition hover:bg-white/[0.08] hover:text-white"
          >
            <Github className="w-4 h-4" />
            <span>View Source on GitHub</span>
          </a>
        </div>

        {/* Trust Badges Bar */}
        <div className="pt-6 flex flex-wrap items-center justify-center gap-x-8 gap-y-3 text-xs text-neutral-400">
          <div className="flex items-center gap-1.5">
            <Cpu className="w-4 h-4 text-pink-400" />
            <span>Offline STT Transcription</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-pink-400" />
            <span>Zero Third-Party Trackers</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Cloud className="w-4 h-4 text-pink-400" />
            <span>Direct Google Drive API</span>
          </div>
          <div className="flex items-center gap-1.5">
            <CheckCircle className="w-4 h-4 text-emerald-400" />
            <span>MIT Open Source</span>
          </div>
        </div>

      </div>
    </section>
  )
}
