//
//  Neumorphic.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

// MARK: - Neumorphic Card Modifier
public struct NeumorphicCardModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    public var cornerRadius: CGFloat
    public var border: Color?

    public init(cornerRadius: CGFloat = 20, border: Color? = nil) {
        self.cornerRadius = cornerRadius
        self.border = border
    }

    public func body(content: Content) -> some View {
        let isDark = colorScheme == .dark
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)

        let topHighlightColor = isDark
            ? Color.white.opacity(0.06)
            : Color.white.opacity(0.9)
        let bottomShadowColor = isDark
            ? Color.black.opacity(0.65)
            : Color(hex: 0x0F172A).opacity(0.08)

        let strokeGradient = LinearGradient(
            colors: [
                isDark ? Color.white.opacity(0.12) : Color.white,
                border ?? (isDark ? Color.obsidianCardBorderDark : Color.obsidianCardBorderLight),
                isDark ? Color.black.opacity(0.3) : Color(hex: 0xCBD5E1).opacity(0.4)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )

        content
            .background(
                shape
                    .fill(Color.obsidianCard)
                    // Top-Left Light Bevel
                    .shadow(color: topHighlightColor, radius: 4, x: -2, y: -2)
                    // Bottom-Right Ambient Drop Shadow
                    .shadow(color: bottomShadowColor, radius: 8, x: 4, y: 5)
            )
            .overlay(
                shape
                    .stroke(strokeGradient, lineWidth: 1)
            )
            .clipShape(shape)
    }
}

// MARK: - Neumorphic Pill Modifier
public struct NeumorphicPillModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    public var cornerRadius: CGFloat
    public var border: Color?

    public init(cornerRadius: CGFloat = 16, border: Color? = nil) {
        self.cornerRadius = cornerRadius
        self.border = border
    }

    public func body(content: Content) -> some View {
        let isDark = colorScheme == .dark
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)

        let highlightColor = isDark
            ? Color.white.opacity(0.05)
            : Color.white.opacity(0.8)
        let shadowColor = isDark
            ? Color.black.opacity(0.45)
            : Color(hex: 0x0F172A).opacity(0.06)

        let strokeGradient = LinearGradient(
            colors: [
                isDark ? Color.white.opacity(0.1) : Color.white,
                border ?? (isDark ? Color.obsidianPillBorderDark : Color.obsidianPillBorderLight)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )

        content
            .background(
                shape
                    .fill(Color.obsidianPill)
                    .shadow(color: highlightColor, radius: 2, x: -1, y: -1)
                    .shadow(color: shadowColor, radius: 4, x: 2, y: 3)
            )
            .overlay(
                shape
                    .stroke(strokeGradient, lineWidth: 1)
            )
            .clipShape(shape)
    }
}

// MARK: - Neumorphic Button Style with Haptics and Spring Depth
public struct NeumorphicButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) private var colorScheme
    public var cornerRadius: CGFloat

    public init(cornerRadius: CGFloat = 14) {
        self.cornerRadius = cornerRadius
    }

    public func makeBody(configuration: Configuration) -> some View {
        let isDark = colorScheme == .dark
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)

        let highlightColor = isDark
            ? Color.white.opacity(configuration.isPressed ? 0.02 : 0.08)
            : Color.white.opacity(configuration.isPressed ? 0.5 : 0.9)
        let shadowColor = isDark
            ? Color.black.opacity(configuration.isPressed ? 0.8 : 0.5)
            : Color(hex: 0x0F172A).opacity(configuration.isPressed ? 0.15 : 0.08)

        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .background(
                shape
                    .fill(Color.obsidianCard)
                    .shadow(
                        color: highlightColor,
                        radius: configuration.isPressed ? 1 : 3,
                        x: configuration.isPressed ? -0.5 : -1.5,
                        y: configuration.isPressed ? -0.5 : -1.5
                    )
                    .shadow(
                        color: shadowColor,
                        radius: configuration.isPressed ? 2 : 6,
                        x: configuration.isPressed ? 1 : 3,
                        y: configuration.isPressed ? 1 : 4
                    )
            )
            .overlay(
                shape
                    .stroke(
                        LinearGradient(
                            colors: [
                                isDark ? Color.white.opacity(0.12) : Color.white,
                                isDark ? Color.obsidianCardBorderDark : Color.obsidianCardBorderLight
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1
                    )
            )
            .animation(.spring(response: 0.25, dampingFraction: 0.7), value: configuration.isPressed)
            .onChange(of: configuration.isPressed) { _, isPressed in
                if isPressed {
                    let generator = UIImpactFeedbackGenerator(style: .light)
                    generator.impactOccurred()
                }
            }
    }
}

// MARK: - View Extension Syntactic Sugar
extension View {
    public func neumorphicCard(cornerRadius: CGFloat = 20, border: Color? = nil) -> some View {
        self.modifier(NeumorphicCardModifier(cornerRadius: cornerRadius, border: border))
    }

    public func neumorphicPill(cornerRadius: CGFloat = 16, border: Color? = nil) -> some View {
        self.modifier(NeumorphicPillModifier(cornerRadius: cornerRadius, border: border))
    }

    public func neumorphicButton(cornerRadius: CGFloat = 14) -> some View {
        self.buttonStyle(NeumorphicButtonStyle(cornerRadius: cornerRadius))
    }
}
