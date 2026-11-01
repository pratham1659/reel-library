import React from 'react'
import { bentoFeatures } from '../data/features'
import { Mic, Sparkles, Search, Cloud, Key, ShieldCheck, Check } from 'lucide-react'

const iconMap: Record<string, React.ReactNode> = {
  Mic: <Mic className="w-5 h-5 text-pink-400" />,
  Sparkles: <Sparkles className="w-5 h-5 text-rose-400" />,
  Search: <Search className="w-5 h-5 text-purple-400" />,
  Cloud: <Cloud className="w-5 h-5 text-pink-400" />,
  Key: <Key className="w-5 h-5 text-amber-400" />,
  ShieldCheck: <ShieldCheck className="w-5 h-5 text-emerald-400" />
}

export const BentoFeatures: React.FC = () => {
  return (
    <section id="features" className="relative py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div className="text-center max-w-2xl mx-auto mb-12">
        <span className="text-xs font-semibold uppercase tracking-wider text-pink-400">
          Core Architecture
        </span>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white mt-1 font-display">
          Engineered for Privacy, Speed & Autonomy
        </h2>
        <p className="text-sm sm:text-base text-neutral-400 mt-3">
          Every component is designed to operate on-device first, keeping your media, keys, 
          and queries strictly under your control.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {bentoFeatures.map((f) => (
          <div
            key={f.id}
            className="group relative rounded-2xl p-6 bg-obsidian-card border border-white/[0.08] hover:border-pink-500/40 transition-all duration-300 hover:shadow-glow-sunset flex flex-col justify-between overflow-hidden"
          >
            {/* Ambient Background Gradient on Card Hover */}
            <div className={`absolute -inset-px rounded-2xl bg-gradient-to-br ${f.gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none -z-10`} />

            <div className="space-y-4">
              {/* Header with Icon & Badge */}
              <div className="flex items-center justify-between">
                <div className="w-10 h-10 rounded-xl bg-white/[0.04] border border-white/[0.08] flex items-center justify-center group-hover:scale-105 transition-transform">
                  {iconMap[f.iconName]}
                </div>
                <span className="text-[10px] font-mono font-medium px-2.5 py-0.5 rounded-full bg-pink-500/10 text-pink-300 border border-pink-500/20">
                  {f.badge}
                </span>
              </div>

              {/* Title & Tagline */}
              <div>
                <h3 className="text-lg font-bold text-white group-hover:text-pink-200 transition-colors">
                  {f.title}
                </h3>
                <div className="text-xs font-medium text-pink-400/90 mt-0.5">
                  {f.tagline}
                </div>
              </div>

              {/* Description */}
              <p className="text-xs sm:text-sm text-neutral-400 leading-relaxed">
                {f.description}
              </p>
            </div>

            {/* Highlights List */}
            <div className="pt-6 mt-6 border-t border-white/[0.06] space-y-1.5">
              {f.highlights.map((h, i) => (
                <div key={i} className="flex items-center gap-2 text-xs text-neutral-300">
                  <Check className="w-3.5 h-3.5 text-pink-400 shrink-0" />
                  <span>{h}</span>
                </div>
              ))}
            </div>

          </div>
        ))}
      </div>
    </section>
  )
}
