//
//  Colors.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

// MARK: - Hex Color Initializer
extension Color {
    public init(hex: UInt32, alpha: Double = 1.0) {
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }

    public init(hexString: String) {
        let hex = hexString.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Obsidian Neumorphic Palette (Parity with Android)
public struct ObsidianPalette {
    // Dark Palette
    public static let obsidianBgDark = Color(hex: 0x0D0E12)
    public static let obsidianCardDark = Color(hex: 0x151821)
    public static let obsidianCardBorderDark = Color(hex: 0x222634)
    public static let obsidianPillDark = Color(hex: 0x1B1F2C)
    public static let obsidianPillBorderDark = Color(hex: 0x2A3044)

    // Light Palette Equivalents
    public static let obsidianBgLight = Color(hex: 0xF8FAFC)
    public static let obsidianCardLight = Color(hex: 0xFFFFFF)
    public static let obsidianCardBorderLight = Color(hex: 0xE2E8F0)
    public static let obsidianPillLight = Color(hex: 0xEEF2F6)
    public static let obsidianPillBorderLight = Color(hex: 0xCBD5E1)

    // Brand & Accents
    public static let sparkleOrange = Color(hex: 0xFF6B4A)
    public static let cyanAccent = Color(hex: 0x00F0FF)
    public static let activeBlue = Color(hex: 0x3877FF)
    public static let electricIndigo = Color(hex: 0x6366F1)

    // Instagram Gradient Colors
    public static let instagramPink = Color(hex: 0xE1306C)
    public static let instagramPurple = Color(hex: 0x833AB4)
    public static let instagramOrange = Color(hex: 0xF77737)

    // Status Indicators
    public static let statusReady = Color(hex: 0x10B981)
    public static let statusProcessing = Color(hex: 0x3B82F6)
    public static let statusError = Color(hex: 0xEF4444)

    // Typography Colors
    public static let textPrimaryDark = Color(hex: 0xF8FAFC)
    public static let textSecondaryDark = Color(hex: 0x94A3B8)
    public static let textMutedDark = Color(hex: 0x64748B)

    public static let textPrimaryLight = Color(hex: 0x0F172A)
    public static let textSecondaryLight = Color(hex: 0x475569)
    public static let textMutedLight = Color(hex: 0x94A3B8)
}

// MARK: - Dynamic Adaptive Colors for Obsidian Dark & Light
extension Color {
    private static func dynamicColor(darkHex: UInt32, lightHex: UInt32) -> Color {
        Color(UIColor { trait in
            trait.userInterfaceStyle == .dark
                ? UIColor(
                    red: CGFloat((darkHex >> 16) & 0xFF) / 255.0,
                    green: CGFloat((darkHex >> 8) & 0xFF) / 255.0,
                    blue: CGFloat(darkHex & 0xFF) / 255.0,
                    alpha: 1.0
                )
                : UIColor(
                    red: CGFloat((lightHex >> 16) & 0xFF) / 255.0,
                    green: CGFloat((lightHex >> 8) & 0xFF) / 255.0,
                    blue: CGFloat(lightHex & 0xFF) / 255.0,
                    alpha: 1.0
                )
        })
    }

    public static let obsidianBg = dynamicColor(darkHex: 0x0D0E12, lightHex: 0xF8FAFC)
    public static let obsidianCard = dynamicColor(darkHex: 0x151821, lightHex: 0xFFFFFF)
    public static let obsidianCardBorder = dynamicColor(darkHex: 0x222634, lightHex: 0xE2E8F0)
    public static let obsidianPill = dynamicColor(darkHex: 0x1B1F2C, lightHex: 0xEEF2F6)
    public static let obsidianPillBorder = dynamicColor(darkHex: 0x2A3044, lightHex: 0xCBD5E1)

    // Static direct aliases
    public static let obsidianBgDark = ObsidianPalette.obsidianBgDark
    public static let obsidianCardDark = ObsidianPalette.obsidianCardDark
    public static let obsidianCardBorderDark = ObsidianPalette.obsidianCardBorderDark
    public static let obsidianPillDark = ObsidianPalette.obsidianPillDark
    public static let obsidianPillBorderDark = ObsidianPalette.obsidianPillBorderDark

    public static let obsidianBgLight = ObsidianPalette.obsidianBgLight
    public static let obsidianCardLight = ObsidianPalette.obsidianCardLight
    public static let obsidianCardBorderLight = ObsidianPalette.obsidianCardBorderLight
    public static let obsidianPillLight = ObsidianPalette.obsidianPillLight
    public static let obsidianPillBorderLight = ObsidianPalette.obsidianPillBorderLight

    // Accents
    public static let sparkleOrange = ObsidianPalette.sparkleOrange
    public static let cyanAccent = ObsidianPalette.cyanAccent
    public static let activeBlue = ObsidianPalette.activeBlue
    public static let electricIndigo = ObsidianPalette.electricIndigo

    // Instagram
    public static let instagramPink = ObsidianPalette.instagramPink
    public static let instagramPurple = ObsidianPalette.instagramPurple
    public static let instagramOrange = ObsidianPalette.instagramOrange

    // Status
    public static let statusReady = ObsidianPalette.statusReady
    public static let statusProcessing = ObsidianPalette.statusProcessing
    public static let statusError = ObsidianPalette.statusError

    // Text
    public static let textPrimary = dynamicColor(darkHex: 0xF8FAFC, lightHex: 0x0F172A)
    public static let textSecondary = dynamicColor(darkHex: 0x94A3B8, lightHex: 0x475569)
    public static let textMuted = dynamicColor(darkHex: 0x64748B, lightHex: 0x94A3B8)

    // Gradients
    public static let instagramGradient = LinearGradient(
        colors: [ObsidianPalette.instagramPurple, ObsidianPalette.instagramPink, ObsidianPalette.instagramOrange],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    public static let sparkleGradient = LinearGradient(
        colors: [Color(hex: 0xFFFF6D00), Color(hex: 0xFFFF3D00)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    public static let cyanBlueGradient = LinearGradient(
        colors: [ObsidianPalette.cyanAccent, ObsidianPalette.activeBlue],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}
