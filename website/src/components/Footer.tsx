import React from 'react'
import { Github, Heart } from 'lucide-react'

export const Footer: React.FC = () => {
  return (
    <footer className="relative border-t border-white/[0.06] bg-obsidian/90 py-12 px-4 sm:px-6 lg:px-8 text-xs text-neutral-400">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-6">
        
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-[#FCAF45] via-[#E1306C] to-[#5851DB] p-[1px]">
            <div className="w-full h-full bg-[#0C0F17] rounded-[7px] flex items-center justify-center p-0.5">
              <svg viewBox="0 0 108 108" className="w-full h-full" fill="none">
                <path d="M38,30 L70,30 A12,12 0 0,1 82,42 L82,66 A12,12 0 0,1 70,78 L38,78 A12,12 0 0,1 26,66 L26,42 A12,12 0 0,1 38,30 Z" stroke="#E1306C" strokeWidth="6" />
                <circle cx="54" cy="54" r="12" stroke="#FFFFFF" strokeWidth="5" />
                <path d="M51,48 L60,54 L51,60 Z" fill="#FFFFFF" />
              </svg>
            </div>
          </div>
          <div>
            <span className="font-bold text-sm text-white">Reel Library</span>
            <span className="ml-2 text-[10px] text-neutral-500">MIT Open Source</span>
          </div>
        </div>

        {/* Links */}
        <div className="flex items-center gap-6">
          <a href="#features" className="hover:text-white transition">Features</a>
          <a href="#demo" className="hover:text-white transition">Demo</a>
          <a href="#cloud-vault" className="hover:text-white transition">Google Drive</a>
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
