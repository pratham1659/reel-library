import React, { useState } from 'react'
import { Download, Github, Menu, X } from 'lucide-react'

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <nav className="sticky top-0 z-40 border-b border-white/[0.06] bg-obsidian/75 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo with Original Sunset Pink App Icon */}
        <a href="#" className="flex items-center gap-2 group shrink-0">
          <img 
            src="/app-icon.svg" 
            alt="Reel Library" 
            className="w-8 h-8 sm:w-9 sm:h-9 rounded-xl shadow-glow-sunset transition transform group-hover:scale-105 shrink-0"
          />
          <div>
            <div className="flex items-center gap-1.5 sm:gap-2">
              <span className="font-display font-bold text-sm sm:text-base tracking-tight text-white whitespace-nowrap">Reel Library</span>
              <span className="hidden xs:inline-block text-[9px] sm:text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/[0.06] text-pink-300 border border-pink-500/20">
                v1.2.3
              </span>
            </div>
          </div>
        </a>

        {/* Desktop Navigation Links */}
        <div className="hidden md:flex items-center gap-8 text-sm font-medium text-neutral-400">
          <a href="#features" className="hover:text-white transition">Features</a>
          <a href="#demo" className="hover:text-white transition">Demo</a>
          <a href="#release-notes" className="hover:text-white transition flex items-center gap-1.5">
            <span>Release Notes</span>
            <span className="text-[9px] px-1.5 py-0.2 rounded bg-pink-500/15 text-pink-300 font-mono">v1.2.3</span>
          </a>
          <a href="#comparison" className="hover:text-white transition">Comparison</a>
          <a href="#faq" className="hover:text-white transition">FAQ</a>
        </div>

        {/* Action CTAs */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
          <a 
            href="https://github.com/pratham1659/reel-library" 
            target="_blank" 
            rel="noopener noreferrer"
            className="hidden sm:inline-flex items-center gap-1.5 rounded-lg border border-white/[0.1] bg-white/[0.03] px-3 py-1.5 text-xs font-medium text-neutral-300 transition hover:bg-white/[0.08] hover:text-white"
          >
            <Github className="w-3.5 h-3.5" />
            <span>GitHub</span>
          </a>

          <a 
            href="/release/ReelLibrary-release-v1.2.3.apk" 
            download="ReelLibrary-release-v1.2.3.apk"
            className="inline-flex items-center gap-1.5 sm:gap-2 rounded-lg bg-gradient-to-r from-[#FF5252] via-[#E1306C] to-[#5851DB] px-2.5 xs:px-3 sm:px-4 py-1.5 text-xs font-semibold text-white shadow-glow-sunset transition hover:brightness-110 active:scale-95 whitespace-nowrap"
          >
            <Download className="w-3.5 h-3.5 shrink-0" />
            <span className="hidden xs:inline">Download</span>
            <span className="hidden sm:inline">&nbsp;APK</span>
            <span className="xs:hidden">APK</span>
          </a>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden text-neutral-400 hover:text-white p-1.5 rounded-lg hover:bg-white/[0.05]"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile dropdown */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-white/[0.06] bg-obsidian-card px-4 py-4 space-y-3">
          <a 
            href="#features" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Features
          </a>
          <a 
            href="#demo" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Interactive Demo
          </a>
          <a 
            href="#release-notes" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Release Notes (v1.2.3)
          </a>
          <a 
            href="#comparison" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Comparison
          </a>
          <a 
            href="#faq" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            FAQ
          </a>
          <div className="pt-2 border-t border-white/[0.06]">
            <a 
              href="https://github.com/pratham1659/reel-library" 
              target="_blank" 
              rel="noopener noreferrer"
              className="flex items-center gap-2 text-sm text-neutral-400 hover:text-white"
            >
              <Github className="w-4 h-4" />
              <span>GitHub Repository</span>
            </a>
          </div>
        </div>
      )}
    </nav>
  )
}
