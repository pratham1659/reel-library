import React from 'react'
import { Share2, Cpu, Sparkles, ArrowRight } from 'lucide-react'

export const HowItWorks: React.FC = () => {
  const steps = [
    {
      number: '01',
      title: 'Share from Instagram',
      description: 'Tap Share on any Instagram Reel and select Reel Library. The app automatically fetches the public post without requiring your personal account login.',
      icon: <Share2 className="w-5 h-5 text-pink-400" />
    },
    {
      number: '02',
      title: 'Local Audio Extraction & STT',
      description: 'The audio stream is isolated directly on your device. Whisper transcribes every spoken word with second-level timestamps in seconds.',
      icon: <Cpu className="w-5 h-5 text-rose-400" />
    },
    {
      number: '03',
      title: 'AI Synthesis & Vault Indexing',
      description: 'Your chosen LLM extracts recipes, code snippets, or itineraries. Everything is indexed into SQLite Room with full-text search and universal export.',
      icon: <Sparkles className="w-5 h-5 text-purple-400" />
    }
  ]

  return (
    <section id="how-it-works" className="relative py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div className="text-center max-w-2xl mx-auto mb-14">
        <span className="text-xs font-semibold uppercase tracking-wider text-pink-400">
          Seamless Workflow
        </span>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white mt-1 font-display">
          From Fleeting Clip to Organized Knowledge
        </h2>
        <p className="text-sm sm:text-base text-neutral-400 mt-3">
          A frictionless 3-step pipeline taking less than 5 seconds from start to finish.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
        {steps.map((s, idx) => (
          <div 
            key={s.number}
            className="relative p-6 rounded-2xl bg-obsidian-card border border-white/[0.08] shadow-neumorphic-sm hover:border-pink-500/30 transition flex flex-col justify-between"
          >
            <div>
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-xl bg-white/[0.04] border border-white/[0.08] flex items-center justify-center">
                  {s.icon}
                </div>
                <span className="font-mono text-2xl font-black text-white/20">
                  {s.number}
                </span>
              </div>

              <h3 className="text-base font-bold text-white mb-2">
                {s.title}
              </h3>
              <p className="text-xs sm:text-sm text-neutral-400 leading-relaxed">
                {s.description}
              </p>
            </div>

            {idx < 2 && (
              <div className="hidden md:block absolute -right-4 top-1/2 -translate-y-1/2 z-10 text-neutral-600">
                <ArrowRight className="w-5 h-5" />
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  )
}
