import React, { useState } from 'react'
import { 
  ShieldCheck, 
  Lock, 
  EyeOff, 
  FileCheck2, 
  Terminal, 
  Copy, 
  Check, 
  AlertTriangle, 
  Network, 
  KeyRound,
  ExternalLink
} from 'lucide-react'

export const SecuritySection: React.FC = () => {
  const [copied, setCopied] = useState(false)
  const sha256Hash = '17225c02bff6f11841734accf55ae5c6941a6c5c6878b43389cce6a71f2420c7'
  const verifyCommand = `sha256sum ReelLibrary-release-v1.2.5.apk`

  const handleCopy = () => {
    navigator.clipboard.writeText(sha256Hash)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const securityPillars = [
    {
      icon: EyeOff,
      title: 'Zero-Telemetry & Offline Vault',
      description: 'Zero Google Analytics, Firebase telemetry, or tracking SDKs. Your reel transcripts, summaries, and SQLite database stay 100% on your device.',
      badge: 'Zero Trackers',
      badgeColor: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20'
    },
    {
      icon: ShieldCheck,
      title: 'Zero-Installer Heuristics',
      description: 'High-risk permissions like REQUEST_INSTALL_PACKAGES and QUERY_ALL_PACKAGES are completely eliminated to prevent Play Protect false alarms.',
      badge: 'Play Protect Clean',
      badgeColor: 'text-pink-400 bg-pink-500/10 border-pink-500/20'
    },
    {
      icon: KeyRound,
      title: 'Hardware-Backed Key Vault',
      description: 'Cloud API keys (Groq, OpenAI, Anthropic) are encrypted at rest with AES-256-GCM backed by Android Keystore (MasterKeys) and iOS Secure Enclave.',
      badge: 'AES-256-GCM',
      badgeColor: 'text-amber-400 bg-amber-500/10 border-amber-500/20'
    },
    {
      icon: Network,
      title: 'Strict TLS Network Security',
      description: 'Enforced via network_security_config.xml with usesCleartextTraffic="false". All unencrypted HTTP traffic is rejected at the OS layer.',
      badge: 'TLS 1.3 Strict',
      badgeColor: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20'
    },
    {
      icon: FileCheck2,
      title: 'Triple Signature Schemes (v1+v2+v3)',
      description: 'Release APKs are cryptographically signed with APK Schemes v1, v2, and v3 simultaneously, ensuring tamper detection across Android 8 through 15.',
      badge: 'v1 + v2 + v3 Signed',
      badgeColor: 'text-purple-400 bg-purple-500/10 border-purple-500/20'
    },
    {
      icon: Lock,
      title: 'Stripped Production Bytecode',
      description: 'All android.util.Log calls and debugging symbols are fully stripped during R8 optimization, guaranteeing zero sensitive endpoint or token leakage.',
      badge: 'R8 ProGuard Stripped',
      badgeColor: 'text-rose-400 bg-rose-500/10 border-rose-500/20'
    }
  ]

  return (
    <section id="security" className="relative py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto overflow-hidden w-full">
      
      {/* Header */}
      <div className="text-center max-w-3xl mx-auto mb-16">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold uppercase tracking-wider mb-3">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Security &amp; Open-Source Trust</span>
        </div>
        <h2 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white font-display">
          Built for Privacy, <br className="hidden sm:inline" />
          <span className="gradient-accent">Hardened for Sideloading</span>
        </h2>
        <p className="mt-4 text-sm sm:text-base text-neutral-400 leading-relaxed">
          Distributed directly via GitHub outside the Google Play Store walled garden. 
          Every permission is audited, every binary is signed, and every line of code is open-source.
        </p>
      </div>

      {/* Security Pillars 6-Card Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        {securityPillars.map((pillar, idx) => {
          const Icon = pillar.icon
          return (
            <div 
              key={idx}
              className="p-6 rounded-2xl bg-obsidian-card border border-white/[0.08] hover:border-pink-500/30 transition-all duration-300 flex flex-col justify-between group hover:shadow-glow-sunset"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="w-10 h-10 rounded-xl bg-white/[0.05] border border-white/[0.1] flex items-center justify-center text-pink-400 group-hover:scale-110 transition-transform">
                    <Icon className="w-5 h-5" />
                  </div>
                  <span className={`text-[10px] font-mono font-semibold px-2.5 py-0.5 rounded-full border ${pillar.badgeColor}`}>
                    {pillar.badge}
                  </span>
                </div>
                <h3 className="text-base font-bold text-white font-display mb-2">
                  {pillar.title}
                </h3>
                <p className="text-xs sm:text-sm text-neutral-400 leading-relaxed">
                  {pillar.description}
                </p>
              </div>
            </div>
          )
        })}
      </div>

      {/* Two Column Row: Cryptographic Verification Box & Play Protect Guidance */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
        
        {/* SHA-256 Checksum Verification Box (7 cols) */}
        <div className="lg:col-span-7 rounded-2xl bg-obsidian-card border border-white/[0.08] p-6 sm:p-7 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <Terminal className="w-4 h-4 text-pink-400" />
              <h3 className="text-sm sm:text-base font-bold text-white font-display">
                Cryptographic Integrity Verification
              </h3>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-pink-500/10 text-pink-300 border border-pink-500/20 ml-auto">
                v1.2.5
              </span>
            </div>
            <p className="text-xs text-neutral-400 mb-4 leading-relaxed">
              Verify your downloaded APK independently against the official hash before installing:
            </p>

            {/* Terminal Command Snippet */}
            <div className="rounded-xl bg-black/60 border border-white/[0.08] p-3.5 font-mono text-xs text-neutral-300 mb-4 space-y-2">
              <div className="text-neutral-500"># Verify using sha256sum:</div>
              <div className="text-emerald-400 select-all overflow-x-auto whitespace-pre">
                $ {verifyCommand}
              </div>
            </div>

            {/* Hash Display & Copy Button */}
            <div className="rounded-xl bg-white/[0.03] border border-white/[0.08] p-3.5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div className="min-w-0 flex-1">
                <span className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider block mb-1">Official SHA-256 Checksum</span>
                <span className="font-mono text-xs text-pink-300 break-all select-all font-semibold">
                  {sha256Hash}
                </span>
              </div>
              <button
                onClick={handleCopy}
                className="shrink-0 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-pink-500/15 hover:bg-pink-500/25 border border-pink-500/30 text-pink-300 text-xs font-semibold transition active:scale-95"
                title="Copy SHA-256 Hash"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copied!' : 'Copy Hash'}</span>
              </button>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-white/[0.05] flex items-center justify-between text-[11px] text-neutral-500">
            <span>Also published in <code className="text-pink-400/90 font-mono">SHA256SUMS.txt</code> on GitHub</span>
            <a 
              href="https://github.com/pratham1659/reel-library/releases/tag/v1.2.5" 
              target="_blank" 
              rel="noopener noreferrer" 
              className="text-pink-400 hover:text-pink-300 inline-flex items-center gap-1 hover:underline"
            >
              <span>Release Notes</span>
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
        </div>

        {/* Play Protect Sideloading Note (5 cols) */}
        <div className="lg:col-span-5 rounded-2xl bg-amber-500/[0.04] border border-amber-500/20 p-6 sm:p-7 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-3 text-amber-400">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <h3 className="text-sm sm:text-base font-bold font-display text-white">
                Google Play Protect Notice
              </h3>
            </div>
            <p className="text-xs text-neutral-300 leading-relaxed mb-4">
              Because Reel Library is distributed directly via GitHub rather than the Google Play Store, Android displays an <strong className="text-white">"Unrecognized app"</strong> prompt during first installation.
            </p>
            <div className="space-y-2.5 text-xs text-neutral-300">
              <div className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-amber-500/20 border border-amber-500/30 text-amber-300 flex items-center justify-center font-bold text-[11px] shrink-0 mt-0.5">1</span>
                <span>Tap <strong className="text-white">"More details"</strong> on the prompt screen.</span>
              </div>
              <div className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-amber-500/20 border border-amber-500/30 text-amber-300 flex items-center justify-center font-bold text-[11px] shrink-0 mt-0.5">2</span>
                <span>Select <strong className="text-white">"Install anyway"</strong> to complete setup.</span>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-amber-500/20 text-[11px] text-amber-300/80">
            ✓ Standard Android security behavior for all self-signed open-source binaries.
          </div>
        </div>

      </div>

    </section>
  )
}
