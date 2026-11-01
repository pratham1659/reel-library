import React, { useState } from 'react'
import { Download, Github, Menu, X } from 'lucide-react'

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <nav className="sticky top-0 z-40 border-b border-white/[0.06] bg-obsidian/75 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo with Obsidian & Electric Cyan App Icon */}
        <a href="#" className="flex items-center gap-3 group">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-[#080C16] via-[#0F2438] to-[#22D3EE] p-[1.5px] shadow-glow-cyan transition transform group-hover:scale-105">
            <div className="w-full h-full bg-[#080C16] rounded-[10.5px] flex items-center justify-center p-1">
              <svg viewBox="0 0 108 108" className="w-full h-full" fill="none">
                <path 
                  d="M38,30 L70,30 A12,12 0 0,1 82,42 L82,66 A12,12 0 0,1 70,78 L38,78 A12,12 0 0,1 26,66 L26,42 A12,12 0 0,1 38,30 Z" 
                  stroke="#22D3EE" 
                  strokeWidth="4.5" 
                />
                <circle cx="54" cy="54" r="12" stroke="#FFFFFF" strokeWidth="4" />
                <path d="M51,48 L60,54 L51,60 Z" fill="#FFFFFF" />
              </svg>
            </div>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-display font-bold text-base tracking-tight text-white">Reel Library</span>
              <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/[0.06] text-cyan-300 border border-cyan-500/20">
                v1.2.2
              </span>
            </div>
          </div>
        </a>

        {/* Desktop Navigation Links */}
        <div className="hidden md:flex items-center gap-8 text-sm font-medium text-neutral-400">
          <a href="#features" className="hover:text-white transition">Features</a>
          <a href="#demo" className="hover:text-white transition">Demo</a>
          <a href="#cloud-vault" className="hover:text-white transition">Google Drive</a>
          <a href="#release-notes" className="hover:text-white transition flex items-center gap-1.5">
            <span>Release Notes</span>
            <span className="text-[9px] px-1.5 py-0.2 rounded bg-cyan-500/15 text-cyan-300 font-mono">v1.2.2</span>
          </a>
          <a href="#comparison" className="hover:text-white transition">Comparison</a>
          <a href="#faq" className="hover:text-white transition">FAQ</a>
        </div>

        {/* Action CTAs */}
        <div className="flex items-center gap-3">
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
            href="#download" 
            className="inline-flex items-center gap-2 rounded-lg bg-gradient-to-r from-cyan-500 to-cyan-600 px-4 py-1.5 text-xs font-semibold text-black shadow-glow-cyan transition hover:brightness-110 active:scale-95"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Get APK</span>
          </a>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden text-neutral-400 hover:text-white p-1"
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
            href="#cloud-vault" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Google Drive Vault
          </a>
          <a 
            href="#release-notes" 
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-neutral-300 hover:text-white py-1"
          >
            Release Notes (v1.2.2)
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
