import React from 'react'
import { Cloud, ShieldCheck, HardDrive, Lock, Zap } from 'lucide-react'

export const GoogleDriveVault: React.FC = () => {
  return (
    <section id="cloud-vault" className="relative py-12 sm:py-16 px-3 sm:px-6 lg:px-8 max-w-7xl mx-auto overflow-hidden">
      <div className="relative rounded-3xl p-5 sm:p-8 md:p-12 bg-gradient-to-br from-obsidian-card via-obsidian-elevated to-obsidian border border-pink-500/20 shadow-glow-sunset overflow-hidden">
        
        {/* Ambient background glow */}
        <div className="absolute top-0 right-0 -mr-20 -mt-20 w-80 h-80 rounded-full bg-pink-500/10 blur-3xl pointer-events-none" />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 sm:gap-10 items-center">
          
          {/* Left: Content */}
          <div className="space-y-6">
            <div className="inline-flex items-center gap-2 rounded-full border border-pink-500/30 bg-pink-500/10 px-3 py-1 text-xs text-pink-300">
              <Cloud className="w-3.5 h-3.5 shrink-0" />
              <span>Personal Cloud Architecture (v1.2.3)</span>
            </div>

            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-white font-display leading-tight">
              Direct Google Drive Integration. <br className="hidden sm:inline" />
              <span className="gradient-accent">Your Personal Cloud Vault.</span>
            </h2>

            <p className="text-sm sm:text-base text-neutral-400 leading-relaxed">
              Never worry about vendor lock-in or subscription storage limits. Reel Library links directly 
              to your Google Account via official Google Sign-In, storing backups strictly in your 
              own Google Drive account.
            </p>

            <div className="space-y-3 pt-2">
              <div className="flex items-start gap-3 text-sm text-neutral-300">
                <div className="w-6 h-6 rounded-lg bg-pink-500/15 flex items-center justify-center shrink-0 mt-0.5">
                  <ShieldCheck className="w-3.5 h-3.5 text-pink-400" />
                </div>
                <div>
                  <span className="font-semibold text-white">Scoped App Authorization: </span>
                  <span className="text-neutral-400">Reel Library requests limited drive.file access—it can only touch files it creates itself.</span>
                </div>
              </div>

              <div className="flex items-start gap-3 text-sm text-neutral-300">
                <div className="w-6 h-6 rounded-lg bg-rose-500/15 flex items-center justify-center shrink-0 mt-0.5">
                  <Lock className="w-3.5 h-3.5 text-rose-400" />
                </div>
                <div>
                  <span className="font-semibold text-white">Zero Intermediary Server: </span>
                  <span className="text-neutral-400">No cloud proxy or third-party server intercepts your tokens or media.</span>
                </div>
              </div>

              <div className="flex items-start gap-3 text-sm text-neutral-300">
                <div className="w-6 h-6 rounded-lg bg-purple-500/15 flex items-center justify-center shrink-0 mt-0.5">
                  <HardDrive className="w-3.5 h-3.5 text-purple-400" />
                </div>
                <div>
                  <span className="font-semibold text-white">Real-Time Storage Telemetry: </span>
                  <span className="text-neutral-400">Live breakdown of audio bytes, transcripts, and SQLite metadata right in Settings.</span>
                </div>
              </div>
            </div>

          </div>

          {/* Right: Technical Diagram Card */}
          <div className="relative p-4 sm:p-6 md:p-8 rounded-2xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-md space-y-5">
            <div className="flex items-center justify-between border-b border-white/[0.06] pb-4">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-red-500/80" />
                <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
                <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
              </div>
              <span className="text-[11px] font-mono text-neutral-400">security_architecture.json</span>
            </div>

            {/* Architecture Flow */}
            <div className="space-y-3 text-xs font-mono">
              <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06] flex flex-col xs:flex-row xs:items-center justify-between gap-1.5">
                <div className="flex items-center gap-2.5">
                  <Zap className="w-4 h-4 text-amber-400 shrink-0" />
                  <span className="text-white">Android Client (Reel Library)</span>
                </div>
                <span className="text-[10px] text-emerald-400">On-Device SQLite</span>
              </div>

              <div className="flex justify-center text-pink-400 text-xs">
                ↓ Direct TLS 1.3 Scoped OAuth2
              </div>

              <div className="p-3 rounded-xl bg-pink-500/[0.08] border border-pink-500/30 flex flex-col xs:flex-row xs:items-center justify-between gap-1.5">
                <div className="flex items-center gap-2.5">
                  <Cloud className="w-4 h-4 text-pink-400 shrink-0" />
                  <span className="text-white">Google Drive API v3</span>
                </div>
                <span className="text-[10px] text-pink-300 font-semibold">User's Private Drive</span>
              </div>

              <div className="p-3 rounded-xl bg-white/[0.02] border border-dashed border-red-500/30 text-[11px] text-neutral-400 text-center">
                ⛔ Zero Third-Party Servers or SaaS Middlemen
              </div>
            </div>

            {/* Storage breakdown preview */}
            <div className="pt-2 border-t border-white/[0.06] space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="text-neutral-400">Storage Telemetry</span>
                <span className="text-white font-mono">142.8 MB Synced</span>
              </div>
              <div className="h-2 rounded-full bg-white/[0.06] flex overflow-hidden">
                <div className="bg-pink-500 h-full w-[45%]" />
                <div className="bg-purple-500 h-full w-[35%]" />
                <div className="bg-amber-400 h-full w-[20%]" />
              </div>
            </div>

          </div>

        </div>

      </div>
    </section>
  )
}
