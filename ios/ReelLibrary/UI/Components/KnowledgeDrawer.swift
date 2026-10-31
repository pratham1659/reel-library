//
//  KnowledgeDrawer.swift
//  ReelLibrary
//

import SwiftUI

public struct KnowledgeDrawer: View {
    @Binding public var isOpen: Bool
    public var totalReels: Int
    public var completedReels: Int
    public var tagCounts: [String: Int]
    public var creatorCounts: [String: Int]
    @Binding public var selectedTag: String?
    @Binding public var selectedCreator: String?
    public var onSelectTag: (String?) -> Void
    public var onSelectCreator: (String?) -> Void

    public init(
        isOpen: Binding<Bool>,
        totalReels: Int,
        completedReels: Int,
        tagCounts: [String: Int],
        creatorCounts: [String: Int],
        selectedTag: Binding<String?>,
        selectedCreator: Binding<String?>,
        onSelectTag: @escaping (String?) -> Void,
        onSelectCreator: @escaping (String?) -> Void
    ) {
        self._isOpen = isOpen
        self.totalReels = totalReels
        self.completedReels = completedReels
        self.tagCounts = tagCounts
        self.creatorCounts = creatorCounts
        self._selectedTag = selectedTag
        self._selectedCreator = selectedCreator
        self.onSelectTag = onSelectTag
        self.onSelectCreator = onSelectCreator
    }

    public var body: some View {
        ZStack(alignment: .leading) {
            // Semi-transparent Dimmed Backdrop
            if isOpen {
                Color.black.opacity(0.6)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            isOpen = false
                        }
                    }
                    .transition(.opacity)
            }

            // Slide-Out Sidebar
            if isOpen {
                drawerContent
                    .frame(width: min(UIScreen.main.bounds.width * 0.82, 330))
                    .frame(maxHeight: .infinity)
                    .background(Color.obsidianBg.ignoresSafeArea())
                    .overlay(
                        Rectangle()
                            .fill(Color.obsidianCardBorder)
                            .frame(width: 1),
                        alignment: .trailing
                    )
                    .transition(.move(edge: .leading))
                    .zIndex(2)
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.8), value: isOpen)
    }

    private var drawerContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    ZStack {
                        Circle()
                            .fill(Color.sparkleGradient)
                            .frame(width: 38, height: 38)
                        Image(systemName: "sparkles")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Reel Library")
                            .font(.appTitleSection)
                            .foregroundStyle(Color.textPrimary)
                        Text("Knowledge Base")
                            .font(.appCaptionSmall)
                            .foregroundStyle(Color.textMuted)
                    }

                    Spacer()

                    Button {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                            isOpen = false
                        }
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundStyle(Color.textSecondary)
                            .padding(8)
                            .background(Circle().fill(Color.obsidianPill))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(20)

            Divider()
                .background(Color.obsidianCardBorder)

            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    // 1. Library Statistics
                    VStack(alignment: .leading, spacing: 10) {
                        sectionLabel("LIBRARY STATS", icon: "chart.bar.xaxis")

                        HStack(spacing: 10) {
                            statCard(title: "Saved", value: "\(totalReels)", color: Color.activeBlue)
                            statCard(title: "Indexed", value: "\(completedReels)", color: Color.statusReady)
                            statCard(title: "Hours Saved", value: String(format: "%.1f", Double(completedReels) * 0.15), color: Color.sparkleOrange)
                        }
                    }

                    // 2. Active Filter Reset Bar (if any)
                    if selectedTag != nil || selectedCreator != nil {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Filtering by:")
                                    .font(.appCaptionSmall)
                                    .foregroundStyle(Color.textMuted)
                                if let tag = selectedTag {
                                    Text("#\(tag)")
                                        .font(.appCaptionPill)
                                        .foregroundStyle(Color.cyanAccent)
                                }
                                if let creator = selectedCreator {
                                    Text(creator)
                                        .font(.appCaptionPill)
                                        .foregroundStyle(Color.instagramPink)
                                }
                            }

                            Spacer()

                            Button("Clear") {
                                onSelectTag(nil)
                                onSelectCreator(nil)
                            }
                            .font(.appCaptionPill)
                            .foregroundStyle(Color.sparkleOrange)
                        }
                        .padding(10)
                        .background(RoundedRectangle(cornerRadius: 10).fill(Color.obsidianCard))
                    }

                    // 3. Tags Collection
                    VStack(alignment: .leading, spacing: 10) {
                        sectionLabel("TOP TAGS (\(tagCounts.count))", icon: "number")

                        if tagCounts.isEmpty {
                            Text("No tags extracted yet.")
                                .font(.appCaptionSmall)
                                .foregroundStyle(Color.textMuted)
                        } else {
                            VStack(spacing: 6) {
                                ForEach(tagCounts.sorted(by: { $0.value > $1.value }).prefix(15), id: \.key) { tag, count in
                                    tagRow(tag: tag, count: count)
                                }
                            }
                        }
                    }

                    // 4. Creators Collection
                    VStack(alignment: .leading, spacing: 10) {
                        sectionLabel("CREATORS (\(creatorCounts.count))", icon: "person.2")

                        if creatorCounts.isEmpty {
                            Text("No creators captured yet.")
                                .font(.appCaptionSmall)
                                .foregroundStyle(Color.textMuted)
                        } else {
                            VStack(spacing: 6) {
                                ForEach(creatorCounts.sorted(by: { $0.value > $1.value }).prefix(15), id: \.key) { creator, count in
                                    creatorRow(creator: creator, count: count)
                                }
                            }
                        }
                    }
                }
                .padding(20)
            }
        }
    }

    private func sectionLabel(_ title: String, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(Color.sparkleOrange)
            Text(title)
                .font(.appCaptionSmall)
                .fontWeight(.bold)
                .foregroundStyle(Color.textMuted)
        }
    }

    private func statCard(title: String, value: String, color: Color) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.appMonoStat)
                .fontWeight(.bold)
                .foregroundStyle(color)
            Text(title)
                .font(.system(size: 10, weight: .medium))
                .foregroundStyle(Color.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.obsidianCard)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.obsidianCardBorder, lineWidth: 1)
        )
    }

    private func tagRow(tag: String, count: Int) -> some View {
        let isSelected = selectedTag == tag
        return Button {
            onSelectTag(isSelected ? nil : tag)
            withAnimation { isOpen = false }
        } label: {
            HStack {
                Text("#\(tag)")
                    .font(.appBodyRegular)
                    .foregroundStyle(isSelected ? Color.cyanAccent : Color.textPrimary)

                Spacer()

                Text("\(count)")
                    .font(.appMonoSmall)
                    .foregroundStyle(Color.textMuted)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Capsule().fill(Color.obsidianPill))
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(isSelected ? Color.cyanAccent.opacity(0.12) : Color.clear)
            )
        }
        .buttonStyle(.plain)
    }

    private func creatorRow(creator: String, count: Int) -> some View {
        let isSelected = selectedCreator == creator
        return Button {
            onSelectCreator(isSelected ? nil : creator)
            withAnimation { isOpen = false }
        } label: {
            HStack {
                Image(systemName: "person.circle.fill")
                    .font(.system(size: 16))
                    .foregroundStyle(Color.instagramPink)

                Text(creator)
                    .font(.appBodyRegular)
                    .foregroundStyle(isSelected ? Color.instagramPink : Color.textPrimary)

                Spacer()

                Text("\(count)")
                    .font(.appMonoSmall)
                    .foregroundStyle(Color.textMuted)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Capsule().fill(Color.obsidianPill))
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(isSelected ? Color.instagramPink.opacity(0.12) : Color.clear)
            )
        }
        .buttonStyle(.plain)
    }
}
