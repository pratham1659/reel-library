//
//  ReelCardView.swift
//  ReelLibrary
//

import SwiftUI

public struct ReelCardView: View {
    public let reel: ReelRecord
    public var viewMode: ViewMode = .grid
    public var isSelectionMode: Bool = false
    public var isSelected: Bool = false
    public var onSelect: (() -> Void)?
    public var onTap: (() -> Void)?

    @Environment(\.colorScheme) private var colorScheme

    public init(
        reel: ReelRecord,
        viewMode: ViewMode = .grid,
        isSelectionMode: Bool = false,
        isSelected: Bool = false,
        onSelect: (() -> Void)? = nil,
        onTap: (() -> Void)? = nil
    ) {
        self.reel = reel
        self.viewMode = viewMode
        self.isSelectionMode = isSelectionMode
        self.isSelected = isSelected
        self.onSelect = onSelect
        self.onTap = onTap
    }

    public var body: some View {
        Group {
            if viewMode == .grid {
                gridCard
            } else {
                listCard
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            if isSelectionMode {
                onSelect?()
            } else {
                onTap?()
            }
        }
    }

    // MARK: - Grid Presentation (2 Columns)
    private var gridCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Thumbnail Area
            ZStack(alignment: .topTrailing) {
                thumbnailView(height: 150)

                // Top Bar inside Thumbnail: Selection checkmark (left) & Status (right)
                HStack {
                    if isSelectionMode {
                        selectionIndicator
                    }
                    Spacer()
                    StatusBadge(status: reel.status, compact: true)
                }
                .padding(8)

                // Play icon overlay in center
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Image(systemName: "play.circle.fill")
                            .font(.system(size: 24))
                            .foregroundStyle(Color.white.opacity(0.85))
                            .shadow(color: .black.opacity(0.6), radius: 4, x: 0, y: 2)
                        Spacer()
                    }
                    Spacer()
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))

            // Text Content
            VStack(alignment: .leading, spacing: 6) {
                // Creator & Date
                HStack {
                    Text(reel.displayCreator)
                        .font(.appCaptionSmall)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color.instagramPink)
                        .lineLimit(1)

                    Spacer()

                    Text(reel.formattedDate)
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.textMuted)
                }

                // Title
                Text(reel.displayTitle)
                    .font(.appTitleCard)
                    .foregroundStyle(Color.textPrimary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                // One-Line Briefing
                if let summary = reel.oneLineSummary, !summary.isEmpty {
                    Text(summary)
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.textSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }

                // Tags Chips
                if !reel.tags.isEmpty {
                    tagChips(limit: 2)
                }
            }
            .padding(.horizontal, 10)
            .padding(.bottom, 12)
        }
        .neumorphicCard(
            cornerRadius: 18,
            border: isSelected ? Color.activeBlue : nil
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(isSelected ? Color.activeBlue : Color.clear, lineWidth: 2)
        )
    }

    // MARK: - List Presentation
    private var listCard: some View {
        HStack(alignment: .top, spacing: 12) {
            // Multi-Select Checkbox
            if isSelectionMode {
                selectionIndicator
                    .padding(.top, 24)
            }

            // Thumbnail
            ZStack(alignment: .topTrailing) {
                thumbnailView(width: 85, height: 110)

                StatusBadge(status: reel.status, compact: true)
                    .padding(5)

                // Center Play Icon
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Image(systemName: "play.circle.fill")
                            .font(.system(size: 18))
                            .foregroundStyle(Color.white.opacity(0.85))
                        Spacer()
                    }
                    Spacer()
                }
            }
            .frame(width: 85, height: 110)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

            // Body
            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Text(reel.displayCreator)
                        .font(.appCaptionSmall)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color.instagramPink)

                    Spacer()

                    Text(reel.formattedDate)
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.textMuted)
                }

                Text(reel.displayTitle)
                    .font(.appTitleCard)
                    .foregroundStyle(Color.textPrimary)
                    .lineLimit(1)

                if let summary = reel.oneLineSummary, !summary.isEmpty {
                    Text(summary)
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.textSecondary)
                        .lineLimit(2)
                }

                Spacer(minLength: 2)

                if !reel.tags.isEmpty {
                    tagChips(limit: 3)
                }
            }
            .padding(.vertical, 8)
            .padding(.trailing, 8)
        }
        .padding(8)
        .neumorphicCard(
            cornerRadius: 18,
            border: isSelected ? Color.activeBlue : nil
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(isSelected ? Color.activeBlue : Color.clear, lineWidth: 2)
        )
    }

    // MARK: - Subviews

    private func thumbnailView(width: CGFloat? = nil, height: CGFloat) -> some View {
        Group {
            if let thumbUrl = reel.thumbnailUrl, let url = URL(string: thumbUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                    case .failure:
                        placeholderThumbnail
                    case .empty:
                        ZStack {
                            placeholderThumbnail
                            ProgressView()
                                .tint(Color.sparkleOrange)
                        }
                    @unknown default:
                        placeholderThumbnail
                    }
                }
            } else {
                placeholderThumbnail
            }
        }
        .frame(maxWidth: width == nil ? .infinity : width, maxHeight: height)
        .clipped()
    }

    private var placeholderThumbnail: some View {
        ZStack {
            LinearGradient(
                colors: [Color.obsidianPill, Color.obsidianCardBorder],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            Image(systemName: "film")
                .font(.system(size: 28))
                .foregroundStyle(Color.textMuted.opacity(0.4))
        }
    }

    private var selectionIndicator: some View {
        Button {
            onSelect?()
        } label: {
            ZStack {
                Circle()
                    .fill(isSelected ? Color.activeBlue : Color.obsidianPill)
                    .frame(width: 24, height: 24)
                    .overlay(
                        Circle()
                            .stroke(isSelected ? Color.white : Color.obsidianCardBorder, lineWidth: 1.5)
                    )

                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Color.white)
                }
            }
        }
        .buttonStyle(.plain)
    }

    private func tagChips(limit: Int) -> some View {
        HStack(spacing: 5) {
            ForEach(reel.tags.prefix(limit), id: \.self) { tag in
                let cleanTag = tag.hasPrefix("#") ? tag : "#\(tag)"
                Text(cleanTag)
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(Color.cyanAccent)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 3)
                    .background(
                        RoundedRectangle(cornerRadius: 6)
                            .fill(Color.cyanAccent.opacity(0.1))
                    )
            }
            if reel.tags.count > limit {
                Text("+\(reel.tags.count - limit)")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(Color.textMuted)
            }
        }
    }
}
