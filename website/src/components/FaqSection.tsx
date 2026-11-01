import React, { useState } from 'react'
import { faqList } from '../data/faq'
import { ChevronDown } from 'lucide-react'

export const FaqSection: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0)

  const toggle = (idx: number) => {
    setOpenIndex(openIndex === idx ? null : idx)
  }

  return (
    <section id="faq" className="relative py-16 px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto">
      <div className="text-center mb-12">
        <span className="text-xs font-semibold uppercase tracking-wider text-cyan-400">
          Frequently Asked Questions
        </span>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white mt-1 font-display">
          Everything You Need to Know
        </h2>
        <p className="text-sm sm:text-base text-neutral-400 mt-3">
          Have more questions? Check our open-source documentation or open a GitHub discussion.
        </p>
      </div>

      <div className="space-y-3">
        {faqList.map((item, idx) => {
          const isOpen = openIndex === idx
          return (
            <div
              key={idx}
              className="rounded-2xl border border-white/[0.08] bg-obsidian-card overflow-hidden transition-all duration-200"
            >
              <button
                onClick={() => toggle(idx)}
                className="w-full py-4 px-6 text-left flex items-center justify-between gap-4 focus:outline-none"
              >
                <span className="text-sm sm:text-base font-semibold text-white">
                  {item.question}
                </span>
                <ChevronDown
                  className={`w-4 h-4 text-cyan-400 transition-transform duration-200 shrink-0 ${
                    isOpen ? 'rotate-180' : ''
                  }`}
                />
              </button>

              {isOpen && (
                <div className="px-6 pb-5 pt-1 text-xs sm:text-sm text-neutral-400 leading-relaxed border-t border-white/[0.04]">
                  {item.answer}
                </div>
              )}
            </div>
          )
        })}
      </div>
    </section>
  )
}
