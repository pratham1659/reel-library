//
//  VaultView.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

public struct VaultView: View {
    @State public var viewModel: VaultViewModel
    @State private var isShowingUrlPrompt = false
    @State private var pastedUrlString = ""

    public init(viewModel: VaultViewModel = VaultViewModel()) {
        _viewModel = State(initialValue: viewModel)
    }

    public var body: some View {
        ZStack {
            Color.obsidianBg.ignoresSafeArea()

            VStack(spacing: 0) {
                // 1. Top Navigation Bar
                topNavigationBar

                // 2. Search & AI Discovery Bar
                searchSection
                    .padding(.horizontal, 16)
                    .padding(.top, 10)
                    .padding(.bottom, 8)

                // 3. Main Virtualized Content
                ZStack(alignment: .bottom) {
                    if viewModel.filteredReels.isEmpty {
                        emptyStateView
                    } else {
                        reelsScrollView
                    }

                    // Floating Dock or Batch Action Bar
                    if viewModel.isSelectionMode {
                        batchActionBar
                            .padding(.horizontal, 16)
                            .padding(.bottom, 16)
                            .transition(.move(edge: .bottom).combined(with: .opacity))
                    } else {
                        FloatingBottomDock(
                            viewMode: $viewModel.viewMode,
                            isSearchActive: !viewModel.searchQuery.isEmpty,
                            hasActiveFilters: viewModel.hasActiveFilters,
                            onToggleSearch: {
                                // Toggle focus or clear search
                                if !viewModel.searchQuery.isEmpty {
                                    viewModel.searchQuery = ""
                                }
                            },
                            onOpenSortFilter: {
                                viewModel.isSortFilterSheetOpen = true
                            },
                            onAddReel: {
                                isShowingUrlPrompt = true
                            }
                        )
                        .padding(.bottom, 16)
                    }
                }
            }

            // Knowledge Drawer Slide-Out Overlay
            KnowledgeDrawer(
                isOpen: $viewModel.isDrawerOpen,
                totalReels: viewModel.totalReelsCount,
                completedReels: viewModel.completedReelsCount,
                tagCounts: viewModel.tagCounts,
                creatorCounts: viewModel.creatorCounts,
                selectedTag: $viewModel.selectedTag,
                selectedCreator: $viewModel.selectedCreator,
                onSelectTag: { tag in
                    viewModel.selectedTag = tag
                },
                onSelectCreator: { creator in
                    viewModel.selectedCreator = creator
                }
            )
        }
        // Sheets & Alerts
        .sheet(item: $viewModel.selectedReelForDetail) { reel in
            ReelDetailModal(
                reel: reel,
                onSaveNotes: { notes in
                    if let id = reel.id {
                        viewModel.saveNotes(id: id, notes: notes)
                    }
                },
                onAddTag: { tag in
                    if let id = reel.id {
                        viewModel.addTag(id: id, tag: tag)
                    }
                },
                onRemoveTag: { tag in
                    if let id = reel.id {
                        viewModel.removeTag(id: id, tag: tag)
                    }
                },
                onReprocess: {
                    if let id = reel.id {
                        viewModel.reprocessReel(id: id)
                    }
                },
                onDelete: {
                    if let id = reel.id {
                        viewModel.deleteReel(id: id)
                    }
                },
                onDismiss: {
                    viewModel.selectedReelForDetail = nil
                }
            )
        }
        .sheet(isPresented: $viewModel.isSortFilterSheetOpen) {
            SortFilterSheet(
                orderBy: $viewModel.orderBy,
                sortDirection: $viewModel.sortDirection,
                selectedStatus: $viewModel.selectedStatus,
                selectedTag: $viewModel.selectedTag,
                availableTags: Array(viewModel.tagCounts.keys),
                onReset: {
                    viewModel.resetFilters()
                },
                onDismiss: {
                    viewModel.isSortFilterSheetOpen = false
                }
            )
        }
        .sheet(isPresented: $viewModel.isSettingsOpen) {
            SettingsView()
        }
        .alert("Add Instagram Reel", isPresented: $isShowingUrlPrompt) {
            TextField("Paste Instagram Reel URL...", text: $pastedUrlString)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            Button("Ingest", action: {
                if !pastedUrlString.isEmpty {
                    viewModel.ingestUrl(pastedUrlString)
                    pastedUrlString = ""
                }
            })
            Button("Paste from Clipboard") {
                if let string = UIPasteboard.general.string {
                    viewModel.ingestUrl(string)
                }
            }
            Button("Cancel", role: .cancel) {
                pastedUrlString = ""
            }
        } message: {
            Text("Enter a public Instagram Reel link to download audio, transcribe, and extract actionable takeaways.")
        }
    }

    // MARK: - Top Navigation Bar
    private var topNavigationBar: some View {
        HStack(spacing: 12) {
            // Knowledge Drawer Toggle Button
            Button {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                    viewModel.isDrawerOpen.toggle()
                }
            } label: {
                Image(systemName: "sidebar.leading")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(Color.textPrimary)
                    .frame(width: 38, height: 38)
                    .background(Circle().fill(Color.obsidianCard))
                    .overlay(Circle().stroke(Color.obsidianCardBorder, lineWidth: 1))
            }
            .buttonStyle(.plain)

            // App Brand Title
            HStack(spacing: 6) {
                Image(systemName: "sparkles")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(Color.sparkleOrange)
                Text("Reel Library")
                    .font(.appTitleSection)
                    .foregroundStyle(Color.textPrimary)
            }

            Spacer()

            // Real-Time Live Ping / Latency Pill
            Text(viewModel.liveLatencyText)
                .font(.appMonoSmall)
                .foregroundStyle(Color.cyanAccent)
                .padding(.horizontal, 9)
                .padding(.vertical, 5)
                .background(Capsule().fill(Color.obsidianPill))
                .overlay(Capsule().stroke(Color.cyanAccent.opacity(0.3), lineWidth: 1))

            // Settings Gear Button
            Button {
                viewModel.isSettingsOpen = true
            } label: {
                Image(systemName: "gearshape")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(Color.textPrimary)
                    .frame(width: 38, height: 38)
                    .background(Circle().fill(Color.obsidianCard))
                    .overlay(Circle().stroke(Color.obsidianCardBorder, lineWidth: 1))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    // MARK: - Search & AI Discovery
    private var searchSection: some View {
        VStack(spacing: 8) {
            HStack(spacing: 10) {
                // Search Input Field
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 15))
                        .foregroundStyle(Color.textMuted)

                    TextField("Search reels, takeaways, creators...", text: $viewModel.searchQuery)
                        .font(.appBodyRegular)
                        .foregroundStyle(Color.textPrimary)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()

                    if !viewModel.searchQuery.isEmpty {
                        Button {
                            viewModel.searchQuery = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 14))
                                .foregroundStyle(Color.textMuted)
                        }
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color.obsidianCard)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color.obsidianCardBorder, lineWidth: 1)
                )

                // AI Search Toggle Button
                Button {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                        viewModel.isAiSearchEnabled.toggle()
                    }
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 12, weight: .bold))
                        Text("AI")
                            .font(.appCaptionPill)
                            .fontWeight(.bold)
                    }
                    .foregroundStyle(viewModel.isAiSearchEnabled ? Color.white : Color.textSecondary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(viewModel.isAiSearchEnabled ? AnyShapeStyle(Color.cyanBlueGradient) : AnyShapeStyle(Color.obsidianCard))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(viewModel.isAiSearchEnabled ? Color.clear : Color.obsidianCardBorder, lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)

                // Select Mode Toggle Button
                Button {
                    withAnimation {
                        viewModel.toggleSelectionMode()
                    }
                } label: {
                    Image(systemName: viewModel.isSelectionMode ? "checkmark.circle.fill" : "checklist")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(viewModel.isSelectionMode ? Color.activeBlue : Color.textSecondary)
                        .frame(width: 38, height: 38)
                        .background(RoundedRectangle(cornerRadius: 14).fill(Color.obsidianCard))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.obsidianCardBorder, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }

            // AI Search Suggestion Chips
            if viewModel.isAiSearchEnabled && !viewModel.aiSearchSuggestions.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(viewModel.aiSearchSuggestions, id: \.self) { suggestion in
                            Button {
                                viewModel.searchQuery = suggestion
                            } label: {
                                HStack(spacing: 4) {
                                    Image(systemName: "sparkle")
                                        .font(.system(size: 9))
                                    Text("#\(suggestion)")
                                        .font(.appCaptionSmall)
                                }
                                .foregroundStyle(Color.cyanAccent)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(Capsule().fill(Color.cyanAccent.opacity(0.12)))
                                .overlay(Capsule().stroke(Color.cyanAccent.opacity(0.3), lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 2)
                }
            }
        }
    }

    // MARK: - Virtualized Content
    private var reelsScrollView: some View {
        ScrollView {
            Group {
                if viewModel.viewMode == .grid {
                    LazyVGrid(
                        columns: [
                            GridItem(.flexible(), spacing: 12),
                            GridItem(.flexible(), spacing: 12)
                        ],
                        spacing: 14
                    ) {
                        ForEach(viewModel.filteredReels) { reel in
                            ReelCardView(
                                reel: reel,
                                viewMode: .grid,
                                isSelectionMode: viewModel.isSelectionMode,
                                isSelected: reel.id.map { viewModel.selectedIds.contains($0) } ?? false,
                                onSelect: {
                                    if let id = reel.id {
                                        viewModel.toggleSelect(id: id)
                                    }
                                },
                                onTap: {
                                    viewModel.selectedReelForDetail = reel
                                }
                            )
                        }
                    }
                } else {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.filteredReels) { reel in
                            ReelCardView(
                                reel: reel,
                                viewMode: .list,
                                isSelectionMode: viewModel.isSelectionMode,
                                isSelected: reel.id.map { viewModel.selectedIds.contains($0) } ?? false,
                                onSelect: {
                                    if let id = reel.id {
                                        viewModel.toggleSelect(id: id)
                                    }
                                },
                                onTap: {
                                    viewModel.selectedReelForDetail = reel
                                }
                            )
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 90) // clearance for floating dock
        }
        .refreshable {
            viewModel.loadReels()
        }
    }

    // MARK: - Empty State
    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Spacer()

            ZStack {
                Circle()
                    .fill(Color.obsidianCard)
                    .frame(width: 88, height: 88)
                    .overlay(Circle().stroke(Color.obsidianCardBorder, lineWidth: 1))

                Image(systemName: "film.stack")
                    .font(.system(size: 38))
                    .foregroundStyle(Color.sparkleOrange)
            }

            VStack(spacing: 6) {
                Text("No Reels Saved Yet")
                    .font(.appTitleSection)
                    .foregroundStyle(Color.textPrimary)

                Text("Share any Instagram Reel via the iOS Share Sheet or paste a URL below to extract instant insights.")
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            }

            Button {
                isShowingUrlPrompt = true
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "plus.circle.fill")
                    Text("Add Your First Reel")
                        .fontWeight(.semibold)
                }
                .font(.appBodyRegular)
                .foregroundStyle(.white)
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(Color.sparkleGradient)
                .clipShape(Capsule())
                .shadow(color: Color.sparkleOrange.opacity(0.4), radius: 8, x: 0, y: 3)
            }
            .buttonStyle(.plain)

            Spacer()
        }
    }

    // MARK: - Multi-Select Batch Action Bar
    private var batchActionBar: some View {
        HStack(spacing: 12) {
            // Select All / Deselect All
            Button {
                viewModel.selectAll()
            } label: {
                Text(viewModel.selectedIds.count == viewModel.filteredReels.count ? "Deselect All" : "Select All")
                    .font(.appCaptionPill)
                    .foregroundStyle(Color.textPrimary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(Color.obsidianPill)
                    .clipShape(Capsule())
            }
            .buttonStyle(.plain)

            Spacer()

            Text("\(viewModel.selectedIds.count) Selected")
                .font(.appCaptionSmall)
                .foregroundStyle(Color.textSecondary)

            Spacer()

            // Delete Selected
            Button {
                viewModel.deleteSelected()
            } label: {
                HStack(spacing: 5) {
                    Image(systemName: "trash")
                    Text("Delete")
                }
                .font(.appCaptionPill)
                .foregroundStyle(viewModel.selectedIds.isEmpty ? Color.textMuted : Color.statusError)
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(Color.obsidianPill)
                .clipShape(Capsule())
            }
            .disabled(viewModel.selectedIds.isEmpty)
            .buttonStyle(.plain)

            // Done / Cancel Selection
            Button {
                viewModel.clearSelection()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Color.textSecondary)
                    .frame(width: 34, height: 34)
                    .background(Circle().fill(Color.obsidianPill))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(
            Capsule()
                .fill(Color.obsidianCard)
                .shadow(color: Color.black.opacity(0.6), radius: 12, x: 0, y: 6)
        )
        .overlay(
            Capsule()
                .stroke(Color.obsidianCardBorder, lineWidth: 1)
        )
    }
}
