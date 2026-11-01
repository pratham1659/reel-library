/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        obsidian: '#090A0E',
        'obsidian-card': '#10131B',
        'obsidian-elevated': '#161B26',
        'obsidian-border': 'rgba(255, 255, 255, 0.08)',
        'obsidian-border-hover': 'rgba(255, 255, 255, 0.16)',
        sunset: {
          gold: '#FCAF45',
          coral: '#FF5252',
          rose: '#E1306C',
          fuchsia: '#C13584',
          indigo: '#5851DB',
        },
        status: {
          ready: '#10B981',
          error: '#EF4444'
        }
      },
      fontFamily: {
        sans: ['Geist', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['Plus Jakarta Sans', 'Geist', 'sans-serif'],
        mono: ['Geist Mono', 'monospace'],
      },
      boxShadow: {
        'glow-sunset': '0 0 35px -5px rgba(225, 48, 108, 0.45)',
        'glow-sunset-lg': '0 0 60px -10px rgba(225, 48, 108, 0.55)',
        'neumorphic-sm': '4px 4px 10px rgba(0, 0, 0, 0.5), -2px -2px 6px rgba(255, 255, 255, 0.03)',
        'neumorphic-md': '8px 8px 20px rgba(0, 0, 0, 0.6), -4px -4px 12px rgba(255, 255, 255, 0.04)',
        'neumorphic-inset': 'inset 3px 3px 8px rgba(0, 0, 0, 0.6), inset -2px -2px 6px rgba(255, 255, 255, 0.03)',
      }
    },
  },
  plugins: [],
}
