import React from 'react'
import { AnnouncementBar } from './components/AnnouncementBar'
import { Navbar } from './components/Navbar'
import { Hero } from './components/Hero'
import { DeviceMockup } from './components/DeviceMockup'
import { BentoFeatures } from './components/BentoFeatures'
import { HowItWorks } from './components/HowItWorks'
import { GoogleDriveVault } from './components/GoogleDriveVault'
import { ReleaseNotes } from './components/ReleaseNotes'
import { ComparisonTable } from './components/ComparisonTable'
import { FaqSection } from './components/FaqSection'
import { DownloadCta } from './components/DownloadCta'
import { Footer } from './components/Footer'

export const App: React.FC = () => {
  return (
    <div className="relative min-h-screen bg-obsidian text-[#EDEDF0] selection:bg-pink-500/30 selection:text-pink-200">
      
      {/* Top Ambient Spotlight Flare with Warm Sunset Aura */}
      <div className="spotlight-glow" aria-hidden="true" />

      {/* Vertical Guide Lines Frame (Inspired by getplexo.app) */}
      <div className="fixed inset-0 pointer-events-none flex justify-center z-0" aria-hidden="true">
        <div className="w-full max-w-7xl h-full border-x border-white/[0.04]" />
      </div>

      <div className="relative z-10 flex flex-col min-h-screen">
        <AnnouncementBar />
        <Navbar />
        
        <main className="flex-1">
          <Hero />
          <DeviceMockup />
          <BentoFeatures />
          <HowItWorks />
          <GoogleDriveVault />
          <ReleaseNotes />
          <ComparisonTable />
          <FaqSection />
          <DownloadCta />
        </main>

        <Footer />
      </div>

    </div>
  )
}

export default App
