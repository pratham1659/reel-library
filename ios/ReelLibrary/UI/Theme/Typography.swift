//
//  Typography.swift
//  ReelLibrary
//

import SwiftUI

extension Font {
    /// Hero title / Display text
    public static let appDisplayLarge = Font.system(size: 28, weight: .bold, design: .default)

    /// Modal header / Major section header
    public static let appDisplayMedium = Font.system(size: 22, weight: .bold, design: .default)

    /// Sub-header / Category title
    public static let appTitleSection = Font.system(size: 18, weight: .semibold, design: .default)

    /// Card title / Reel title
    public static let appTitleCard = Font.system(size: 15, weight: .semibold, design: .default)

    /// Primary body reading text
    public static let appBodyLead = Font.system(size: 15, weight: .medium, design: .default)

    /// Regular body text
    public static let appBodyRegular = Font.system(size: 14, weight: .regular, design: .default)

    /// Pill badge / Tag label text
    public static let appCaptionPill = Font.system(size: 12, weight: .semibold, design: .default)

    /// Secondary footnote / Timestamp label
    public static let appCaptionSmall = Font.system(size: 11, weight: .medium, design: .default)

    /// Telemetry & latency monospace counter
    public static let appMonoStat = Font.system(size: 13, weight: .semibold, design: .monospaced)

    /// Small numeric / badge monospace label
    public static let appMonoSmall = Font.system(size: 11, weight: .medium, design: .monospaced)
}

// MARK: - ViewModifier Helpers for Typography Color Application
public struct PrimaryTextModifier: ViewModifier {
    public func body(content: Content) -> some View {
        content
            .foregroundStyle(Color.textPrimary)
    }
}

public struct SecondaryTextModifier: ViewModifier {
    public func body(content: Content) -> some View {
        content
            .foregroundStyle(Color.textSecondary)
    }
}

public struct MutedTextModifier: ViewModifier {
    public func body(content: Content) -> some View {
        content
            .foregroundStyle(Color.textMuted)
    }
}

extension View {
    public func primaryText() -> some View {
        modifier(PrimaryTextModifier())
    }

    public func secondaryText() -> some View {
        modifier(SecondaryTextModifier())
    }

    public func mutedText() -> some View {
        modifier(MutedTextModifier())
    }
}
