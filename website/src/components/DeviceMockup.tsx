import React, { useState } from 'react'
import { 
  Search, Mic, Sparkles, Cloud, Play, Volume2, 
  Copy, Check, ShieldCheck, Database, RefreshCw, Cpu, Clock
} from 'lucide-react'

export const DeviceMockup: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'vault' | 'transcript' | 'models' | 'cloud'>('vault')
  const [copied, setCopied] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')

  const handleCopy = () => {
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <section id="demo" className="relative py-12 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      {/* Section Header */}
      <div className="text-center max-w-2xl mx-auto mb-10">
        <span className="text-xs font-semibold uppercase tracking-wider text-pink-400">
          Interactive Experience
        </span>
        <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-white mt-1 font-display">
          See Reel Library in Action
        </h2>
        <p className="text-sm text-neutral-400 mt-2">
          Click the feature tabs below to preview the native Android interface, 
          from instantaneous speech transcription to multi-model AI reasoning.
        </p>

        {/* Tab Controls */}
        <div className="mt-6 inline-flex p-1 rounded-xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-inset flex-wrap justify-center gap-1">
          <button
            onClick={() => setActiveTab('vault')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'vault'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Search className="w-3.5 h-3.5" />
            <span>Vault & Search</span>
          </button>

          <button
            onClick={() => setActiveTab('transcript')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'transcript'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Mic className="w-3.5 h-3.5" />
            <span>Audio & Transcript</span>
          </button>

          <button
            onClick={() => setActiveTab('models')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'models'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Multi-LLM Engine</span>
          </button>

          <button
            onClick={() => setActiveTab('cloud')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-medium transition ${
              activeTab === 'cloud'
                ? 'bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] text-white font-semibold shadow-glow-sunset'
                : 'text-neutral-400 hover:text-white hover:bg-white/[0.04]'
            }`}
          >
            <Cloud className="w-3.5 h-3.5" />
            <span>Google Drive Vault</span>
          </button>
        </div>
      </div>

      {/* Phone Bezel Container */}
      <div className="relative mx-auto max-w-[340px] sm:max-w-[380px]">
        {/* Outer Phone Ambient Shadow */}
        <div className="absolute -inset-4 rounded-[50px] bg-gradient-to-b from-pink-500/20 via-purple-500/10 to-transparent blur-2xl -z-10" />

        {/* Physical Bezel */}
        <div className="relative rounded-[44px] p-3 bg-neutral-900 border-[3px] border-neutral-700/80 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9)]">
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
                <span className="font-mono text-pink-400">100%</span>
              </div>
            </div>

            {/* In-App Content based on active tab */}
            <div className="min-h-[500px] p-4 flex flex-col justify-between">
              
              {/* TAB 1: VAULT & SEARCH */}
              {activeTab === 'vault' && (
                <div className="space-y-3.5 animate-fadeIn">
                  {/* App Bar Header */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-gradient-to-tr from-[#FCAF45] via-[#E1306C] to-[#5851DB] p-[1px]">
                        <div className="w-full h-full bg-[#0C0F17] rounded-[5px] flex items-center justify-center">
                          <Play className="w-2.5 h-2.5 fill-white text-white" />
                        </div>
                      </div>
                      <span className="font-bold text-sm font-display tracking-tight text-white">Reel Vault</span>
                    </div>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                      42 Reels Indexed
                    </span>
                  </div>

                  {/* Search Input Mock */}
                  <div className="relative">
                    <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
                    <input
                      type="text"
                      placeholder="Search transcripts, recipes, tags..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="w-full pl-8 pr-3 py-2 rounded-xl bg-obsidian-card border border-white/[0.08] text-[11px] text-neutral-200 placeholder-neutral-500 focus:outline-none focus:border-pink-500/50"
                    />
                  </div>

                  {/* Filter Pills */}
                  <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-[10px]">
                    <span className="px-2.5 py-1 rounded-full bg-pink-500/20 text-pink-300 border border-pink-500/30 whitespace-nowrap font-medium">All Reels</span>
                    <span className="px-2.5 py-1 rounded-full bg-white/[0.04] text-neutral-400 border border-white/[0.06] whitespace-nowrap">🍳 Recipes</span>
                    <span className="px-2.5 py-1 rounded-full bg-white/[0.04] text-neutral-400 border border-white/[0.06] whitespace-nowrap">💻 Tech</span>
                    <span className="px-2.5 py-1 rounded-full bg-white/[0.04] text-neutral-400 border border-white/[0.06] whitespace-nowrap">✈️ Travel</span>
                  </div>

                  {/* Reel Card 1 */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-sm space-y-2 hover:border-pink-500/30 transition">
                    <div className="flex items-center justify-between text-[10px] text-neutral-400">
                      <span className="font-medium text-neutral-300">@chef_antonio • 54s</span>
                      <span className="text-emerald-400 flex items-center gap-1">
                        <CheckCircleIcon className="w-3 h-3" /> Transcribed
                      </span>
                    </div>
                    <div className="text-xs font-semibold text-white">
                      Creamy Tuscan Garlic Chicken in 15 Minutes
                    </div>
                    <p className="text-[11px] text-neutral-400 line-clamp-2 leading-relaxed">
                      "Heat olive oil in a skillet, sear seasoned chicken breasts 5 mins each side. Deglaze with sun-dried tomatoes, heavy cream, garlic, and baby spinach..."
                    </p>
                    <div className="flex items-center gap-1.5 pt-1">
                      <span className="text-[9px] px-2 py-0.5 rounded bg-pink-500/10 text-pink-300 font-mono">#dinner</span>
                      <span className="text-[9px] px-2 py-0.5 rounded bg-white/[0.04] text-neutral-400 font-mono">#recipe</span>
                      <span className="text-[9px] px-2 py-0.5 rounded bg-purple-500/10 text-purple-300 font-mono">Gemini 1.5</span>
                    </div>
                  </div>

                  {/* Reel Card 2 */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-sm space-y-2 opacity-80">
                    <div className="flex items-center justify-between text-[10px] text-neutral-400">
                      <span className="font-medium text-neutral-300">@dev_insights • 38s</span>
                      <span className="text-emerald-400 flex items-center gap-1">
                        <CheckCircleIcon className="w-3 h-3" /> Transcribed
                      </span>
                    </div>
                    <div className="text-xs font-semibold text-white">
                      3 Git Commands Every Senior Engineer Uses
                    </div>
                    <p className="text-[11px] text-neutral-400 line-clamp-2 leading-relaxed">
                      "Use git commit --fixup combined with autosquash, git rerere to cache merge resolutions, and git worktree to switch context instantly without stashing..."
                    </p>
                  </div>
                </div>
              )}

              {/* TAB 2: AUDIO & TRANSCRIPT */}
              {activeTab === 'transcript' && (
                <div className="space-y-3.5 animate-fadeIn">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-pink-500/20 flex items-center justify-center text-pink-400">
                        <Mic className="w-3.5 h-3.5" />
                      </div>
                      <span className="font-bold text-sm text-white">Whisper Speech-to-Text</span>
                    </div>
                    <span className="text-[9px] px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                      Local Offline
                    </span>
                  </div>

                  {/* Audio Waveform Card */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2.5">
                    <div className="flex items-center justify-between text-[10px]">
                      <span className="text-neutral-400 flex items-center gap-1">
                        <Volume2 className="w-3 h-3 text-pink-400" /> audio_track.aac
                      </span>
                      <span className="font-mono text-neutral-400">00:45 / 00:54</span>
                    </div>

                    {/* Simulated Waveform Bars */}
                    <div className="h-8 flex items-center justify-between gap-1 px-1 bg-obsidian/60 rounded-lg">
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

                  {/* Timestamped Transcript Preview */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-[11px]">
                      <span className="font-semibold text-neutral-300">Timestamped Transcript</span>
                      <button 
                        onClick={handleCopy}
                        className="text-[10px] text-pink-400 hover:text-pink-300 flex items-center gap-1"
                      >
                        {copied ? <Check className="w-3 h-3" /> : <Copy className="w-3 h-3" />}
                        <span>{copied ? 'Copied' : 'Copy'}</span>
                      </button>
                    </div>

                    <div className="p-2.5 rounded-xl bg-obsidian-elevated border border-white/[0.06] font-mono text-[10px] text-neutral-300 space-y-1.5 leading-relaxed max-h-48 overflow-y-auto">
                      <div><span className="text-pink-400">[00:00 - 00:08]</span> "Today I am showing you the fastest one-pan garlic Tuscan chicken recipe."</div>
                      <div><span className="text-pink-400">[00:08 - 00:22]</span> "Start with 2 chicken breasts butterflied. Season both sides with smoked paprika, salt, and oregano."</div>
                      <div><span className="text-pink-400">[00:22 - 00:37]</span> "In a hot skillet with olive oil, sear 5 minutes each side until golden brown crust forms."</div>
                      <div><span className="text-pink-400">[00:37 - 00:54]</span> "Lower the heat, pour in heavy cream and garlic, add spinach and sun-dried tomatoes. Simmer 3 mins."</div>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 3: MULTI-LLM ENGINE */}
              {activeTab === 'models' && (
                <div className="space-y-3.5 animate-fadeIn">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-purple-500/20 flex items-center justify-center text-purple-400">
                        <Sparkles className="w-3.5 h-3.5" />
                      </div>
                      <span className="font-bold text-sm text-white">Multi-LLM Engine</span>
                    </div>
                    <span className="text-[9px] px-2 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                      BYOK
                    </span>
                  </div>

                  {/* Model Selector Card */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2">
                    <span className="text-[10px] font-medium text-neutral-400 uppercase tracking-wider">Active Inference Provider</span>
                    
                    {/* Active Gemini */}
                    <div className="p-2 rounded-xl bg-white/[0.04] border border-pink-500/30 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="w-5 h-5 rounded-md bg-gradient-to-r from-blue-500 to-indigo-500 flex items-center justify-center text-[10px] font-bold text-white">G</div>
                        <div>
                          <div className="text-[11px] font-semibold text-white">Google Gemini 1.5 Flash</div>
                          <div className="text-[9px] text-neutral-400">Active • Ultra-fast • Free Tier</div>
                        </div>
                      </div>
                      <span className="text-[9px] px-2 py-0.5 rounded bg-emerald-500/15 text-emerald-300 font-mono">Connected</span>
                    </div>

                    {/* Providers Grid */}
                    <div className="grid grid-cols-3 gap-1.5 text-[9px] text-center pt-1">
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-300">
                        Groq Llama-3
                      </div>
                      <div className="p-1.5 rounded-lg bg-obsidian-elevated border border-white/[0.06] text-neutral-300">
                        OpenAI GPT-4o
                      </div>
                      <div className="p-1.5 rounded-lg bg-purple-500/10 border border-purple-500/30 text-purple-300 relative group">
                        <div className="font-semibold flex items-center justify-center gap-1">
                          <Clock className="w-2.5 h-2.5" /> Local LLM
                        </div>
                        <div className="text-[8px] text-purple-400 font-mono">Coming Soon</div>
                      </div>
                    </div>
                  </div>

                  {/* AI Structured Summary Card */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2">
                    <div className="flex items-center justify-between text-[11px]">
                      <span className="font-semibold text-pink-400 flex items-center gap-1">
                        <Cpu className="w-3.5 h-3.5" /> Structured Output
                      </span>
                      <span className="text-[9px] text-neutral-500 font-mono">0.38s latency</span>
                    </div>
                    <div className="text-[11px] text-neutral-300 space-y-1 leading-snug">
                      <div className="font-semibold text-white">🛒 Ingredients Needed:</div>
                      <p className="text-[10px] text-neutral-400">• 2 Chicken Breasts • 1 cup Heavy Cream • Sun-dried tomatoes • Fresh Baby Spinach</p>
                      <div className="font-semibold text-white pt-1">⚡ Key Technique:</div>
                      <p className="text-[10px] text-neutral-400">Deglaze hot pan fond with cream on low heat to preserve emulsion.</p>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 4: GOOGLE DRIVE VAULT */}
              {activeTab === 'cloud' && (
                <div className="space-y-3.5 animate-fadeIn">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded-md bg-blue-500/20 flex items-center justify-center text-blue-400">
                        <Cloud className="w-3.5 h-3.5" />
                      </div>
                      <span className="font-bold text-sm text-white">Google Drive Personal Vault</span>
                    </div>
                    <span className="text-[9px] px-2 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                      Scoped API
                    </span>
                  </div>

                  {/* Connected Account */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-2.5">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-amber-400 to-pink-500 flex items-center justify-center font-bold text-xs text-white">
                          P
                        </div>
                        <div>
                          <div className="text-[11px] font-semibold text-white">user@gmail.com</div>
                          <div className="text-[9px] text-neutral-400 flex items-center gap-1">
                            <ShieldCheck className="w-2.5 h-2.5 text-emerald-400" />
                            Direct OAuth2 Scoped Token
                          </div>
                        </div>
                      </div>
                      <button className="text-[9px] px-2 py-1 rounded bg-white/[0.06] text-neutral-300 hover:text-white flex items-center gap-1">
                        <RefreshCw className="w-2.5 h-2.5" /> Sync
                      </button>
                    </div>

                    {/* Storage Breakdown Real-time */}
                    <div className="space-y-1.5 pt-1">
                      <div className="flex items-center justify-between text-[10px]">
                        <span className="text-neutral-400 flex items-center gap-1">
                          <Database className="w-3 h-3 text-pink-400" /> Real-time Storage
                        </span>
                        <span className="font-mono text-neutral-200">142.8 MB / 15 GB</span>
                      </div>
                      <div className="h-2 w-full rounded-full bg-white/[0.06] overflow-hidden flex">
                        <div style={{ width: '45%' }} className="bg-pink-500 h-full" title="Reels Audio" />
                        <div style={{ width: '30%' }} className="bg-purple-500 h-full" title="Transcripts" />
                        <div style={{ width: '15%' }} className="bg-amber-400 h-full" title="SQLite Index" />
                      </div>
                      <div className="flex items-center justify-between text-[8px] text-neutral-500">
                        <span>■ Audio (64 MB)</span>
                        <span>■ Transcripts (42 MB)</span>
                        <span>■ Metadata (36 MB)</span>
                      </div>
                    </div>
                  </div>

                  {/* Security Highlights */}
                  <div className="p-3 rounded-2xl bg-obsidian-card border border-white/[0.08] space-y-1.5 text-[10px] text-neutral-300">
                    <div className="font-semibold text-white flex items-center gap-1 text-[11px]">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                      Zero Intermediate Servers
                    </div>
                    <p className="text-neutral-400 text-[10px] leading-relaxed">
                      Uploads travel strictly between your smartphone and Google Drive via encrypted HTTPS. No company servers ever touch your files.
                    </p>
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

function CheckCircleIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 20 20" fill="currentColor">
      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
    </svg>
  )
}
