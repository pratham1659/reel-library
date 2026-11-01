import React from 'react'
import { comparisonData } from '../data/comparison'
import { Check, X } from 'lucide-react'

export const ComparisonTable: React.FC = () => {
  const renderCell = (val: string | boolean, isHighlighted = false) => {
    if (typeof val === 'boolean') {
      return val ? (
        <span className={`inline-flex items-center justify-center w-5 h-5 rounded-full ${
          isHighlighted ? 'bg-pink-500/20 text-pink-400' : 'bg-emerald-500/20 text-emerald-400'
        }`}>
          <Check className="w-3.5 h-3.5" />
        </span>
      ) : (
        <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-red-500/10 text-red-400">
          <X className="w-3.5 h-3.5" />
        </span>
      )
    }
    return <span className="text-xs text-neutral-400">{val}</span>
  }

  return (
    <section id="comparison" className="relative py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div className="text-center max-w-2xl mx-auto mb-12">
        <span className="text-xs font-semibold uppercase tracking-wider text-pink-400">
          Uncompromised Design
        </span>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white mt-1 font-display">
          How Reel Library Compares
        </h2>
        <p className="text-sm sm:text-base text-neutral-400 mt-3">
          See why standard saved collections fall short and why on-device AI changes everything.
        </p>
      </div>

      {/* Mobile scroll hint */}
      <div className="md:hidden flex items-center justify-center gap-1.5 text-[11px] text-pink-400/90 pb-3">
        <span>👈 Swipe horizontally to view full comparison 👉</span>
      </div>

      <div className="overflow-x-auto rounded-2xl border border-white/[0.08] bg-obsidian-card shadow-neumorphic-sm max-w-full touch-pan-x">
        <table className="w-full text-left border-collapse min-w-[560px] sm:min-w-[640px]">
          <thead>
            <tr className="border-b border-white/[0.08] bg-white/[0.02]">
              <th className="py-4 px-6 text-xs font-semibold uppercase tracking-wider text-neutral-400">
                Capability
              </th>
              <th className="py-4 px-6 text-xs font-semibold uppercase tracking-wider text-pink-400 bg-pink-500/[0.04]">
                Reel Library
              </th>
              <th className="py-4 px-6 text-xs font-semibold uppercase tracking-wider text-neutral-400">
                Instagram Saved
              </th>
              <th className="py-4 px-6 text-xs font-semibold uppercase tracking-wider text-neutral-400">
                Generic Note Apps
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.04] text-sm">
            {comparisonData.map((row, idx) => (
              <tr key={idx} className="hover:bg-white/[0.02] transition">
                <td className="py-3.5 px-6 font-medium text-neutral-200">
                  {row.feature}
                </td>
                <td className="py-3.5 px-6 font-semibold text-white bg-pink-500/[0.04]">
                  {renderCell(row.reelLibrary, true)}
                </td>
                <td className="py-3.5 px-6">
                  {renderCell(row.instagramSaved)}
                </td>
                <td className="py-3.5 px-6">
                  {renderCell(row.genericNotes)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
