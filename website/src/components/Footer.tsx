import React from 'react'
import { Github, Heart } from 'lucide-react'

export const Footer: React.FC = () => {
  return (
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
            <span className="ml-2 text-[10px] text-neutral-500">MIT Open Source</span>
          </div>
        </div>

        {/* Links */}
        <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2.5 text-center">
          <a href="#features" className="hover:text-white transition">Features</a>
          <a href="#demo" className="hover:text-white transition">Demo</a>
          <a href="#release-notes" className="hover:text-white transition">Release Notes</a>
          <a href="#faq" className="hover:text-white transition">FAQ</a>
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

        {/* Copyright */}
        <div className="flex items-center gap-1 text-neutral-500">
          <span>Crafted with</span>
          <Heart className="w-3 h-3 text-pink-500 fill-pink-500" />
          <span>for creators & learners.</span>
        </div>

      </div>
    </footer>
  )
}
