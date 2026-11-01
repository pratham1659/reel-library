import React, { useState } from 'react'
import { 
  Share2, Mic, Zap, FileText, Volume2, 
  Copy, Check, ArrowRight, CheckCircle2
} from 'lucide-react'

export const DeviceMockup: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'share' | 'audio' | 'models' | 'summary'>('share')
  const [copied, setCopied] = useState(false)

  const handleCopy = () => {
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <section id="demo" className="relative py-12 px-3 sm:px-6 lg:px-8 max-w-7xl mx-auto overflow-hidden">
      {/* Section Header */}
      <div className="text-center max-w-2xl mx-auto mb-10">
        <span className="text-xs font-semibold uppercase tracking-wider text-pink-400">
          Interactive Experience
        </span>
        <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-white mt-1 font-display">
          See Reel Library in Action
        </h2>
        <p className="text-sm text-neutral-400 mt-2 px-2 sm:px-0">
          Click the feature tabs below to preview the native Android interface, 
          from 1-tap Instagram sharing to offline Whisper audio and Groq AI summaries.
        </p>

        {/* Tab Controls (4 responsive tabs strictly in 1 single row on all mobile viewports) */}
        <div className="mt-6 grid grid-cols-4 p-1 rounded-xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-inset gap-1 w-full max-w-xl mx-auto">
          <button
            onClick={() => setActiveTab('share')}
            className={`flex items-center justify-center gap-1 sm:gap-1.5 py-2 px-1 sm:px-2 rounded-lg text-[10px] sm:text-xs font-medium transition ${
              activeTab === 'share'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Share2 className="w-3.5 h-3.5 shrink-0" />
            <span className="sm:hidden">Share</span>
            <span className="hidden sm:inline">Instagram</span>
          </button>

          <button
            onClick={() => setActiveTab('audio')}
            className={`flex items-center justify-center gap-1 sm:gap-1.5 py-2 px-1 sm:px-2 rounded-lg text-[10px] sm:text-xs font-medium transition ${
              activeTab === 'audio'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Mic className="w-3.5 h-3.5 shrink-0" />
            <span className="sm:hidden">Audio</span>
            <span className="hidden sm:inline">Audio & Notes</span>
          </button>

          <button
            onClick={() => setActiveTab('models')}
            className={`flex items-center justify-center gap-1 sm:gap-1.5 py-2 px-1 sm:px-2 rounded-lg text-[10px] sm:text-xs font-medium transition ${
              activeTab === 'models'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Zap className="w-3.5 h-3.5 shrink-0" />
            <span className="sm:hidden">Groq AI</span>
            <span className="hidden sm:inline">Groq Engine</span>
          </button>

          <button
            onClick={() => setActiveTab('summary')}
            className={`flex items-center justify-center gap-1 sm:gap-1.5 py-2 px-1 sm:px-2 rounded-lg text-[10px] sm:text-xs font-medium transition ${
              activeTab === 'summary'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <FileText className="w-3.5 h-3.5 shrink-0" />
            <span className="sm:hidden">Summary</span>
            <span className="hidden sm:inline">Summary Drawer</span>
          </button>
        </div>
      </div>

      {/* Phone Bezel Container */}
      <div className="relative mx-auto w-full max-w-[300px] xs:max-w-[310px] sm:max-w-[360px] md:max-w-[380px]">
        {/* Outer Phone Ambient Shadow */}
        <div className="absolute -inset-1 sm:-inset-4 rounded-[50px] bg-gradient-to-b from-pink-500/20 via-purple-500/10 to-transparent blur-xl sm:blur-2xl -z-10" />

        {/* Physical Bezel */}
        <div className="relative rounded-[44px] p-2.5 sm:p-3 bg-neutral-900 border-[3px] border-neutral-700/80 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9)]">
          {/* Antenna bands */}
          <div className="absolute top-24 -left-1.5 w-1 h-8 bg-neutral-700 rounded-l" />
          <div className="absolute top-36 -left-1.5 w-1 h-12 bg-neutral-700 rounded-l" />
          <div className="absolute top-28 -right-1.5 w-1 h-14 bg-neutral-700 rounded-r" />

          {/* Screen Inner */}
          <div className="relative rounded-[34px] bg-[#0A0D14] overflow-hidden border border-white/[0.06] text-white text-xs select-none">
            
            {/* Camera Punchhole & Status Bar */}
            <div className="h-9 px-6 flex items-center justify-between text-[11px] text-neutral-400 bg-[#0A0D14]/90 backdrop-blur z-20">
              <span className="font-semibold text-neutral-200">09:41</span>
              <div className="w-3.5 h-3.5 rounded-full bg-black border border-neutral-800 flex items-center justify-center">
                <div className="w-1.5 h-1.5 rounded-full bg-neutral-900" />
              </div>
              <div className="flex items-center gap-1.5 text-[10px]">
                <span>5G</span>
                <span className="font-semibold text-pink-400">100%</span>
              </div>
            </div>

            {/* In-App Content based on active tab */}
            <div className="min-h-[510px] p-4 flex flex-col justify-between">
              
              {/* TAB 1: SHARE INSTAGRAM */}
              {activeTab === 'share' && (
                <div className="space-y-3 animate-fadeIn">
                  {/* Header */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-gradient-to-tr from-yellow-500 via-pink-500 to-purple-600 flex items-center justify-center text-white shadow-sm">
                        <Share2 className="w-3.5 h-3.5" />
                      </div>
                      <span className="font-bold text-sm text-white font-display">1-Tap Share Ingestion</span>
                    </div>
                    <span className="text-[9px] font-semibold px-2 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                      Android Intent
                    </span>
                  </div>

                  {/* Simulated Shared Reel Card */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-sm space-y-2">
                    <div className="flex items-center justify-between text-[10px] text-neutral-400">
                      <span className="font-medium text-neutral-300">@chef_antonio • 54s</span>
                      <span className="text-pink-400 text-[9px] font-medium">Instagram Reel</span>
                    </div>

                    <div className="rounded-xl bg-neutral-900/80 border border-white/[0.06] p-2.5 flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-pink-500/30 to-purple-600/30 flex items-center justify-center shrink-0 border border-pink-500/30">
                        <span className="text-base">🍗</span>
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="text-xs font-semibold text-white truncate">Creamy Tuscan Garlic Chicken</div>
                        <div className="text-[10px] text-neutral-400 truncate">instagram.com/reel/C8x9L2p...</div>
                      </div>
                    </div>

                    {/* Native Share Sheet Toast */}
                    <div className="p-2 rounded-xl bg-emerald-500/10 border border-emerald-500/25 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span className="text-[10px] font-medium text-emerald-300">Shared to Reel Library</span>
                      </div>
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-semibold">
                        Instant
                      </span>
                    </div>
                  </div>

                  {/* Automated Background Pipeline */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2">
                    <span className="text-[10px] font-semibold text-neutral-400 uppercase tracking-wider block">
                      Local Background Pipeline
                    </span>

                    <div className="space-y-1.5 text-[10px]">
                      <div className="flex items-center justify-between text-neutral-300">
                        <span className="flex items-center gap-1.5">
                          <Check className="w-3 h-3 text-emerald-400" /> Caption & Metadata Saved
                        </span>
                        <span className="text-emerald-400 font-medium">Done</span>
                      </div>
                      <div className="flex items-center justify-between text-neutral-300">
                        <span className="flex items-center gap-1.5">
                          <Check className="w-3 h-3 text-emerald-400" /> Audio Stream Extracted
                        </span>
                        <span className="text-emerald-400 font-medium">Done</span>
                      </div>
                      <div className="flex items-center justify-between text-neutral-300">
                        <span className="flex items-center gap-1.5">
                          <Check className="w-3 h-3 text-emerald-400" /> Whisper STT Transcription
                        </span>
                        <span className="text-emerald-400 font-medium">Done</span>
                      </div>
                    </div>

                    <button
                      onClick={() => setActiveTab('audio')}
                      className="w-full mt-2 py-1.5 rounded-lg bg-white/[0.06] hover:bg-white/[0.1] text-[11px] font-semibold text-pink-300 flex items-center justify-center gap-1.5 transition"
                    >
                      <span>Inspect Audio & Notes</span>
                      <ArrowRight className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              )}

              {/* TAB 2: AUDIO & CAPTION NOTES */}
              {activeTab === 'audio' && (
                <div className="space-y-3 animate-fadeIn">
                  {/* Header */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-pink-500/20 flex items-center justify-center text-pink-400">
                        <Mic className="w-3.5 h-3.5" />
                      </div>
                      <span className="font-bold text-sm text-white">Audio & Caption Notes</span>
                    </div>
                    <span className="text-[9px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                      100% Offline
                    </span>
                  </div>

                  {/* Audio Waveform Card */}
                  <div className="p-2.5 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2">
                    <div className="flex items-center justify-between text-[10px]">
                      <span className="text-neutral-400 flex items-center gap-1.5">
                        <Volume2 className="w-3 h-3 text-pink-400" /> audio_track.aac
                      </span>
                      <span className="text-neutral-400 font-medium">00:45 / 00:54</span>
                    </div>

                    {/* Waveform Bars */}
                    <div className="h-7 flex items-center justify-between gap-1 px-1 bg-obsidian/60 rounded-lg">
                      {[40, 70, 95, 60, 30, 85, 100, 75, 45, 90, 65, 80, 55, 90, 70, 40, 85, 95, 60, 30, 75, 50, 65, 85, 40].map((h, i) => (
                        <div
                          key={i}
                          style={{ height: `${h}%` }}
                          className={`w-1 rounded-full transition-all ${
                            i < 18 ? 'bg-gradient-to-t from-[#FF5252] to-[#E1306C]' : 'bg-neutral-700'
                          }`}
                        />
                      ))}
                    </div>
                  </div>

                  {/* Extracted Caption Note Card */}
                  <div className="p-2.5 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-1.5">
                    <div className="flex items-center justify-between text-[10px]">
                      <span className="font-semibold text-neutral-300 flex items-center gap-1.5">
                        <FileText className="w-3 h-3 text-pink-400" /> Original Post Caption
                      </span>
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-white/[0.04] text-neutral-400 border border-white/[0.06]">
                        Instagram Meta
                      </span>
                    </div>
                    <p className="text-[10px] text-neutral-300 leading-relaxed">
                      "Tuscan Garlic Chicken 🍗 15-minute weeknight winner! High protein, restaurant quality in one skillet."
                    </p>
                    <div className="flex flex-wrap gap-1 pt-0.5">
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-pink-500/10 text-pink-300">#dinner</span>
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-pink-500/10 text-pink-300">#recipe</span>
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-pink-500/10 text-pink-300">#onepan</span>
                    </div>
                  </div>

                  {/* Timestamped Transcript Preview (Clean Two-Column Alignment) */}
                  <div className="p-2.5 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2">
                    <div className="flex items-center justify-between text-[10px]">
                      <span className="font-semibold text-neutral-300">Whisper Transcript</span>
                      <button 
                        onClick={handleCopy}
                        className="text-[9px] text-pink-400 hover:text-pink-300 flex items-center gap-1 font-medium"
                      >
                        {copied ? <Check className="w-2.5 h-2.5" /> : <Copy className="w-2.5 h-2.5" />}
                        <span>{copied ? 'Copied' : 'Copy'}</span>
                      </button>
                    </div>

                    <div className="space-y-1.5 max-h-28 overflow-y-auto pr-1">
                      <div className="flex items-start gap-1.5 text-[10px]">
                        <span className="text-pink-400 font-semibold bg-pink-500/10 px-1 py-0.5 rounded text-[8px] shrink-0 pt-0.5">
                          00:00
                        </span>
                        <p className="text-neutral-300 leading-snug">Today I am showing you the fastest one-pan garlic Tuscan chicken recipe.</p>
                      </div>
                      <div className="flex items-start gap-1.5 text-[10px]">
                        <span className="text-pink-400 font-semibold bg-pink-500/10 px-1 py-0.5 rounded text-[8px] shrink-0 pt-0.5">
                          00:08
                        </span>
                        <p className="text-neutral-300 leading-snug">Season butterflied chicken with smoked paprika, salt, and oregano.</p>
                      </div>
                      <div className="flex items-start gap-1.5 text-[10px]">
                        <span className="text-pink-400 font-semibold bg-pink-500/10 px-1 py-0.5 rounded text-[8px] shrink-0 pt-0.5">
                          00:22
                        </span>
                        <p className="text-neutral-300 leading-snug">Sear in hot olive oil for 5 minutes per side until golden brown.</p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 3: MULTI-LLM ENGINE (GROQ API LOCKED) */}
              {activeTab === 'models' && (
                <div className="space-y-3 animate-fadeIn">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-purple-500/20 flex items-center justify-center text-purple-400">
                        <Zap className="w-3.5 h-3.5 text-pink-400" />
                      </div>
                      <span className="font-bold text-sm text-white">Multi-LLM Engine</span>
                    </div>
                    <span className="text-[9px] font-semibold px-2 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                      BYOK
                    </span>
                  </div>

                  {/* Active Groq Provider Card */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-pink-500/30 space-y-2">
                    <span className="text-[9px] font-semibold text-neutral-400 uppercase tracking-wider block">
                      Active Inference Provider
                    </span>
                    
                    <div className="p-2.5 rounded-xl bg-white/[0.04] border border-pink-500/30 flex items-center justify-between">
                      <div className="flex items-center gap-2.5">
                        <div className="w-6 h-6 rounded-lg bg-gradient-to-tr from-[#F03850] to-[#C81466] flex items-center justify-center text-[10px] font-bold text-white shadow-sm">
                          G
                        </div>
                        <div>
                          <div className="text-[11px] font-semibold text-white">Groq LPU™ API</div>
                          <div className="text-[9px] text-neutral-400">Qwen 2.5 72B • 165ms Latency</div>
                        </div>
                      </div>
                      <span className="text-[9px] px-2 py-0.5 rounded bg-emerald-500/15 text-emerald-300 font-semibold border border-emerald-500/30">
                        Connected
                      </span>
                    </div>

                    {/* Engine Telemetry */}
                    <div className="grid grid-cols-3 gap-1.5 text-[9px] text-center pt-0.5">
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-300">
                        <div className="text-neutral-400 text-[8px]">Latency</div>
                        <div className="font-semibold text-pink-300">165 ms</div>
                      </div>
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-300">
                        <div className="text-neutral-400 text-[8px]">Speed</div>
                        <div className="font-semibold text-pink-300">285 t/s</div>
                      </div>
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-300">
                        <div className="text-neutral-400 text-[8px]">Status</div>
                        <div className="font-semibold text-emerald-400">Online</div>
                      </div>
                    </div>
                  </div>

                  {/* Multi-Engine Fleet Switcher */}
                  <div className="p-2.5 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-1.5">
                    <span className="text-[9px] font-semibold text-neutral-400 uppercase tracking-wider block">
                      Supported BYOK Providers
                    </span>
                    <div className="grid grid-cols-3 gap-1.5 text-[9px] text-center">
                      <div className="p-1.5 rounded-lg bg-pink-500/15 border border-pink-500/30 text-pink-300 font-semibold">
                        Groq (Active)
                      </div>
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-400">
                        OpenAI GPT-4o
                      </div>
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-400">
                        DeepSeek V3
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 4: SUMMARY DRAWER */}
              {activeTab === 'summary' && (
                <div className="space-y-2.5 animate-fadeIn">
                  {/* Drawer Drag Bar */}
                  <div className="w-10 h-1 rounded-full bg-neutral-600 mx-auto" />

                  {/* Drawer Header */}
                  <div className="flex items-center justify-between pb-0.5">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-gradient-to-r from-[#F03850] to-[#C81466] flex items-center justify-center text-white">
                        <FileText className="w-3.5 h-3.5" />
                      </div>
                      <div>
                        <div className="font-bold text-xs text-white">AI Summary Drawer</div>
                        <div className="text-[9px] text-neutral-400">Groq Qwen 2.5 • Instant Extraction</div>
                      </div>
                    </div>
                    <button
                      onClick={handleCopy}
                      className="text-[9px] text-pink-400 hover:text-pink-300 flex items-center gap-1 font-medium bg-white/[0.04] px-2 py-1 rounded-md border border-white/[0.06]"
                    >
                      {copied ? <Check className="w-2.5 h-2.5" /> : <Copy className="w-2.5 h-2.5" />}
                      <span>{copied ? 'Copied' : 'Copy'}</span>
                    </button>
                  </div>

                  {/* Executive Briefing */}
                  <div className="p-2.5 rounded-xl bg-obsidian-card border border-white/[0.08] space-y-1">
                    <div className="text-[9px] font-semibold text-pink-400 uppercase tracking-wider">
                      Executive Briefing
                    </div>
                    <p className="text-[10px] text-neutral-300 leading-snug">
                      A fast 15-minute Tuscan chicken skillet featuring seared chicken cutlets simmered in a luscious garlic, heavy cream, and spinach sauce.
                    </p>
                  </div>

                  {/* Key Ingredients Pills */}
                  <div className="p-2.5 rounded-xl bg-obsidian-card border border-white/[0.08] space-y-1.5">
                    <div className="text-[9px] font-semibold text-pink-400 uppercase tracking-wider">
                      Key Ingredients
                    </div>
                    <div className="grid grid-cols-2 gap-1 text-[9px]">
                      <span className="p-1 rounded bg-white/[0.04] text-neutral-300 border border-white/[0.06] truncate">• 2 Chicken Breasts</span>
                      <span className="p-1 rounded bg-white/[0.04] text-neutral-300 border border-white/[0.06] truncate">• 1 cup Heavy Cream</span>
                      <span className="p-1 rounded bg-white/[0.04] text-neutral-300 border border-white/[0.06] truncate">• 4 Garlic Cloves</span>
                      <span className="p-1 rounded bg-white/[0.04] text-neutral-300 border border-white/[0.06] truncate">• Spinach & Tomatoes</span>
                    </div>
                  </div>

                  {/* Actionable Steps (Cleanly Aligned Row Layout) */}
                  <div className="p-2.5 rounded-xl bg-obsidian-card border border-white/[0.08] space-y-1.5">
                    <div className="text-[9px] font-semibold text-pink-400 uppercase tracking-wider">
                      Preparation Steps
                    </div>
                    <div className="space-y-1.5 text-[10px]">
                      <div className="flex items-start gap-2">
                        <span className="w-4 h-4 rounded-full bg-pink-500/20 text-pink-300 font-bold text-[9px] flex items-center justify-center shrink-0 mt-0.5">
                          1
                        </span>
                        <p className="text-neutral-300 leading-tight">
                          <strong className="text-white">Season & Sear:</strong> Brown seasoned chicken cutlets 5 mins per side in olive oil.
                        </p>
                      </div>
                      <div className="flex items-start gap-2">
                        <span className="w-4 h-4 rounded-full bg-pink-500/20 text-pink-300 font-bold text-[9px] flex items-center justify-center shrink-0 mt-0.5">
                          2
                        </span>
                        <p className="text-neutral-300 leading-tight">
                          <strong className="text-white">Make Sauce:</strong> Sauté garlic & tomatoes; pour cream on low heat to preserve emulsion.
                        </p>
                      </div>
                      <div className="flex items-start gap-2">
                        <span className="w-4 h-4 rounded-full bg-pink-500/20 text-pink-300 font-bold text-[9px] flex items-center justify-center shrink-0 mt-0.5">
                          3
                        </span>
                        <p className="text-neutral-300 leading-tight">
                          <strong className="text-white">Simmer:</strong> Fold in baby spinach and simmer chicken 3 mins until rich.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Bottom Gesture Bar */}
              <div className="pt-2 flex justify-center">
                <div className="w-28 h-1 rounded-full bg-neutral-600" />
              </div>

            </div>

            {/* Glass reflection overlay */}
            <div className="phone-glass-reflection absolute inset-0 pointer-events-none" />

          </div>
        </div>
      </div>
    </section>
  )
}
