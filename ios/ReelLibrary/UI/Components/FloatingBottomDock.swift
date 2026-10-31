//
//  FloatingBottomDock.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

public struct FloatingBottomDock: View {
    @Binding public var viewMode: ViewMode
    public var isSearchActive: Bool
    public var hasActiveFilters: Bool
    public var onToggleSearch: () -> Void
    public var onOpenSortFilter: () -> Void
    public var onAddReel: () -> Void

    public init(
        viewMode: Binding<ViewMode>,
        isSearchActive: Bool,
        hasActiveFilters: Bool,
        onToggleSearch: @escaping () -> Void,
        onOpenSortFilter: @escaping () -> Void,
        onAddReel: @escaping () -> Void
    ) {
        self._viewMode = viewMode
        self.isSearchActive = isSearchActive
        self.hasActiveFilters = hasActiveFilters
        self.onToggleSearch = onToggleSearch
        self.onOpenSortFilter = onOpenSortFilter
        self.onAddReel = onAddReel
    }

    public var body: some View {
        HStack(spacing: 20) {
            // 1. Search Button
            Button {
                triggerHaptic()
                onToggleSearch()
            } label: {
                Image(systemName: isSearchActive ? "magnifyingglass.circle.fill" : "magnifyingglass")
                    .font(.system(size: 19, weight: .semibold))
                    .foregroundStyle(isSearchActive ? Color.cyanAccent : Color.textSecondary)
                    .frame(width: 40, height: 40)
            }
            .buttonStyle(.plain)

            // 2. View Mode Toggle (Grid vs List)
            Button {
                triggerHaptic()
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    viewMode = (viewMode == .grid) ? .list : .grid
                }
            } label: {
                Image(systemName: viewMode == .grid ? "list.bullet" : "square.grid.2x2")
                    .font(.system(size: 19, weight: .semibold))
                    .foregroundStyle(Color.textSecondary)
                    .frame(width: 40, height: 40)
            }
            .buttonStyle(.plain)

            // 3. Sort & Filter with Active Dot Badge
            Button {
                triggerHaptic()
                onOpenSortFilter()
            } label: {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundStyle(hasActiveFilters ? Color.sparkleOrange : Color.textSecondary)
                        .frame(width: 40, height: 40)

                    if hasActiveFilters {
                        Circle()
                            .fill(Color.sparkleOrange)
                            .frame(width: 8, height: 8)
                            .offset(x: -6, y: 6)
                    }
                }
            }
            .buttonStyle(.plain)

            // Divider
            Rectangle()
                .fill(Color.obsidianPillBorder)
                .frame(width: 1, height: 26)

            // 4. Primary "+" Add / Ingest Button
            Button {
                triggerHaptic(style: .medium)
                onAddReel()
            } label: {
                ZStack {
                    Circle()
                        .fill(Color.sparkleGradient)
                        .frame(width: 44, height: 44)
                        .shadow(color: Color.sparkleOrange.opacity(0.4), radius: 8, x: 0, y: 3)

                    Image(systemName: "plus")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(.white)
                }
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(
            Capsule()
                .fill(Color.obsidianPill.opacity(0.92))
                .shadow(color: Color.black.opacity(0.45), radius: 16, x: 0, y: 8)
        )
        .overlay(
            Capsule()
                .stroke(
                    LinearGradient(
                        colors: [
                            Color.white.opacity(0.15),
                            Color.obsidianPillBorder,
                            Color.black.opacity(0.3)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        )
    }

    private func triggerHaptic(style: UIImpactFeedbackGenerator.FeedbackStyle = .light) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.impactOccurred()
    }
}
