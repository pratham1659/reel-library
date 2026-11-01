import React, { useState } from 'react'
import { Github, Heart, Scale, X, ExternalLink } from 'lucide-react'

export const Footer: React.FC = () => {
  const [isLicenseOpen, setIsLicenseOpen] = useState(false)

  return (
    <>
      <footer className="relative border-t border-white/[0.06] bg-obsidian/90 py-12 px-4 sm:px-6 lg:px-8 text-xs text-neutral-400">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-6">
          
          {/* Brand */}
          <div className="flex items-center gap-3">
            <img 
              src="/app-icon.svg" 
              alt="Reel Library" 
              className="w-8 h-8 rounded-xl shadow-glow-sunset"
            />
            <div>
              <span className="font-bold text-sm text-white">Reel Library</span>
              <button 
                onClick={() => setIsLicenseOpen(true)}
                className="ml-2 inline-flex items-center gap-1 text-[10px] text-pink-400/90 hover:text-pink-300 font-mono px-1.5 py-0.5 rounded bg-pink-500/10 border border-pink-500/20 transition hover:bg-pink-500/20"
                title="View MIT License"
              >
                <Scale className="w-3 h-3" />
                <span>MIT License</span>
              </button>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2.5 text-center">
            <a href="#features" className="hover:text-white transition">Features</a>
            <a href="#demo" className="hover:text-white transition">Demo</a>
            <a href="#release-notes" className="hover:text-white transition">Release Notes</a>
            <a href="#faq" className="hover:text-white transition">FAQ</a>
            <button 
              onClick={() => setIsLicenseOpen(true)} 
              className="hover:text-white transition"
            >
              MIT License
            </button>
            <a 
              href="https://github.com/pratham1659/reel-library" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-white transition flex items-center gap-1"
            >
              <Github className="w-3.5 h-3.5" />
              <span>Source Code</span>
            </a>
          </div>

          {/* Author Branding / Crafted With */}
          <div className="flex items-center gap-1.5 text-neutral-400">
            <span>crafted with</span>
            <Heart className="w-3.5 h-3.5 text-pink-500 fill-pink-500" />
            <a 
              href="https://github.com/pratham1659" 
              target="_blank" 
              rel="noopener noreferrer" 
              className="font-semibold text-pink-400 hover:text-pink-300 transition"
            >
              pratham builds
            </a>
          </div>

        </div>
      </footer>

      {/* MIT License Modal Dialog */}
      {isLicenseOpen && (
        <div 
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn"
          onClick={() => setIsLicenseOpen(false)}
        >
          <div 
            className="relative w-full max-w-xl max-h-[85vh] rounded-3xl bg-obsidian-card border border-pink-500/30 p-6 sm:p-8 shadow-[0_0_80px_rgba(225,48,108,0.25)] flex flex-col justify-between overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className="flex items-center justify-between pb-4 border-b border-white/[0.08]">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-pink-500/10 border border-pink-500/20 flex items-center justify-center text-pink-400">
                  <Scale className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white font-display">MIT License</h3>
                  <p className="text-[11px] text-neutral-400 font-mono">Reel Library • Free & Open Source</p>
                </div>
              </div>
              <button 
                onClick={() => setIsLicenseOpen(false)}
                className="p-1.5 rounded-lg text-neutral-400 hover:text-white hover:bg-white/[0.06] transition"
                aria-label="Close license dialog"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* License Text */}
            <div className="my-4 overflow-y-auto max-h-[50vh] pr-1">
              <pre className="font-mono text-[11px] sm:text-xs text-neutral-300 bg-black/40 p-4 rounded-xl border border-white/[0.06] whitespace-pre-wrap leading-relaxed select-text">
{`MIT License

Copyright (c) 2026 Pratham Kumar (pratham builds)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.`}
              </pre>
            </div>

            {/* Footer Actions */}
            <div className="pt-3 border-t border-white/[0.08] flex items-center justify-between gap-3 text-xs">
              <a 
                href="/LICENSE" 
                target="_blank" 
                rel="noopener noreferrer" 
                className="inline-flex items-center gap-1.5 text-pink-400 hover:text-pink-300 font-medium"
              >
                <span>Raw License File</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </a>

              <button
                onClick={() => setIsLicenseOpen(false)}
                className="px-4 py-1.5 rounded-lg bg-white/[0.08] text-white hover:bg-white/[0.12] transition font-medium text-xs"
              >
                Close
              </button>
            </div>

          </div>
        </div>
      )}
    </>
  )
}
