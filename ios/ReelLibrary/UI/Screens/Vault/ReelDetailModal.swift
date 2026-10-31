//
//  ReelDetailModal.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

public struct ReelDetailModal: View {
    public let reel: ReelRecord
    public var onSaveNotes: (String) -> Void
    public var onAddTag: (String) -> Void
    public var onRemoveTag: (String) -> Void
    public var onReprocess: () -> Void
    public var onDelete: () -> Void
    public var onDismiss: () -> Void

    @State private var notesText: String = ""
    @State private var newTagInput: String = ""
    @State private var checkedTakeaways: Set<Int> = []
    @State private var isShowingDeleteConfirmation: Bool = false
    @State private var hasCopiedTranscript: Bool = false

    public init(
        reel: ReelRecord,
        onSaveNotes: @escaping (String) -> Void,
        onAddTag: @escaping (String) -> Void,
        onRemoveTag: @escaping (String) -> Void,
        onReprocess: @escaping () -> Void,
        onDelete: @escaping () -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.reel = reel
        self.onSaveNotes = onSaveNotes
        self.onAddTag = onAddTag
        self.onRemoveTag = onRemoveTag
        self.onReprocess = onReprocess
        self.onDelete = onDelete
        self.onDismiss = onDismiss
        _notesText = State(initialValue: reel.userNotes ?? "")
    }

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    // 1. Header Information
                    headerSection

                    // 2. Video Thumbnail Preview
                    thumbnailSection

                    // 3. Title & Executive Briefing Card
                    executiveBriefingCard

                    // 4. Actionable Takeaways Checklist
                    takeawaysSection

                    // 5. Full Audio Transcript Reader
                    transcriptSection

                    // 6. Personal Markdown Notes Editor
                    notesSection

                    // 7. Tags Manager
                    tagsSection

                    // 8. Management Actions (Reprocess & Delete)
                    actionButtonsSection

                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(Color.obsidianBg.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button {
                        onDismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 20))
                            .foregroundStyle(Color.textMuted)
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    StatusBadge(status: reel.status)
                }
            }
            .confirmationDialog(
                "Delete Reel?",
                isPresented: $isShowingDeleteConfirmation,
                titleVisibility: .visible
            ) {
                Button("Delete Reel", role: .destructive) {
                    onDelete()
                    onDismiss()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("Are you sure you want to delete this Reel and its extracted knowledge? This cannot be undone.")
            }
        }
    }

    // MARK: - Header Section
    private var headerSection: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 3) {
                Text(reel.displayCreator)
                    .font(.appTitleSection)
                    .foregroundStyle(Color.instagramPink)

                Text("Saved \(reel.formattedDate)")
                    .font(.appCaptionSmall)
                    .foregroundStyle(Color.textMuted)
            }

            Spacer()

            if let url = URL(string: reel.canonicalUrl) {
                Link(destination: url) {
                    HStack(spacing: 5) {
                        Image(systemName: "arrow.up.right.square")
                            .font(.system(size: 13, weight: .semibold))
                        Text("Open Reel")
                            .font(.appCaptionPill)
                    }
                    .foregroundStyle(Color.textPrimary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color.obsidianPill)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(Color.obsidianPillBorder, lineWidth: 1)
                    )
                }
            }
        }
    }

    // MARK: - Thumbnail Preview
    private var thumbnailSection: some View {
        ZStack(alignment: .center) {
            if let thumbUrl = reel.thumbnailUrl, let url = URL(string: thumbUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                    default:
                        placeholderPreview
                    }
                }
            } else {
                placeholderPreview
            }

            // Play / Instagram Icon Button
            if let url = URL(string: reel.canonicalUrl) {
                Link(destination: url) {
                    Circle()
                        .fill(Color.black.opacity(0.65))
                        .frame(width: 54, height: 54)
                        .overlay(
                            Image(systemName: "play.fill")
                                .font(.system(size: 22))
                                .foregroundStyle(.white)
                                .offset(x: 2)
                        )
                }
            }
        }
        .frame(height: 200)
        .frame(maxWidth: .infinity)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.obsidianCardBorder, lineWidth: 1)
        )
    }

    private var placeholderPreview: some View {
        LinearGradient(
            colors: [Color.obsidianCard, Color.obsidianPill],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    // MARK: - Executive Briefing Card
    private var executiveBriefingCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                Image(systemName: "sparkles")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Color.sparkleOrange)
                Text("EXECUTIVE BRIEFING")
                    .font(.appCaptionSmall)
                    .fontWeight(.bold)
                    .foregroundStyle(Color.sparkleOrange)
            }

            Text(reel.displayTitle)
                .font(.appDisplayMedium)
                .foregroundStyle(Color.textPrimary)

            if let summary = reel.oneLineSummary, !summary.isEmpty {
                Text(summary)
                    .font(.appBodyLead)
                    .foregroundStyle(Color.textSecondary)
                    .lineSpacing(4)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .neumorphicCard(cornerRadius: 18)
    }

    // MARK: - Takeaways Checklist
    private var takeawaysSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(title: "ACTIONABLE TAKEAWAYS (\(reel.takeaways.count))", icon: "checklist")

            if reel.takeaways.isEmpty {
                Text("No takeaways generated for this reel.")
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textMuted)
            } else {
                VStack(spacing: 8) {
                    ForEach(Array(reel.takeaways.enumerated()), id: \.offset) { index, takeaway in
                        let isChecked = checkedTakeaways.contains(index)
                        Button {
                            withAnimation(.spring(response: 0.25, dampingFraction: 0.7)) {
                                if isChecked {
                                    checkedTakeaways.remove(index)
                                } else {
                                    checkedTakeaways.insert(index)
                                    let generator = UIImpactFeedbackGenerator(style: .medium)
                                    generator.impactOccurred()
                                }
                            }
                        } label: {
                            HStack(alignment: .top, spacing: 12) {
                                Image(systemName: isChecked ? "checkmark.circle.fill" : "circle")
                                    .font(.system(size: 20))
                                    .foregroundStyle(isChecked ? Color.statusReady : Color.textMuted)
                                    .padding(.top, 2)

                                Text(takeaway)
                                    .font(.appBodyRegular)
                                    .foregroundStyle(isChecked ? Color.textMuted : Color.textPrimary)
                                    .strikethrough(isChecked, color: Color.textMuted)
                                    .multilineTextAlignment(.leading)
                                    .lineSpacing(3)

                                Spacer()
                            }
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(isChecked ? Color.obsidianCard.opacity(0.5) : Color.obsidianCard)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(isChecked ? Color.statusReady.opacity(0.3) : Color.obsidianCardBorder, lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    // MARK: - Transcript Reader
    private var transcriptSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                sectionHeader(title: "FULL AUDIO TRANSCRIPT", icon: "waveform")

                Spacer()

                if TranscriptOutcome.hasSpokenTranscript(reel.transcript), let transcript = reel.transcript {
                    Text("\(transcript.count) chars")
                        .font(.appMonoSmall)
                        .foregroundStyle(Color.textMuted)

                    Button {
                        UIPasteboard.general.string = transcript
                        hasCopiedTranscript = true
                        let generator = UINotificationFeedbackGenerator()
                        generator.notificationOccurred(.success)
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                            hasCopiedTranscript = false
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: hasCopiedTranscript ? "checkmark" : "doc.on.doc")
                                .font(.system(size: 11, weight: .bold))
                            Text(hasCopiedTranscript ? "Copied" : "Copy")
                                .font(.appCaptionSmall)
                                .fontWeight(.semibold)
                        }
                        .foregroundStyle(hasCopiedTranscript ? Color.statusReady : Color.activeBlue)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 4)
                        .background(Capsule().fill(Color.obsidianPill))
                    }
                    .buttonStyle(.plain)
                }
            }

            if TranscriptOutcome.hasSpokenTranscript(reel.transcript), let transcript = reel.transcript {
                Text(transcript)
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textSecondary)
                    .lineSpacing(5)
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(Color.obsidianCard)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(Color.obsidianCardBorder, lineWidth: 1)
                    )
            } else {
                Text("Not available")
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textMuted)
                Button("Retry transcription") {
                    onReprocess()
                }
                .font(.appCaptionSmall)
                .fontWeight(.semibold)
                .foregroundStyle(Color.activeBlue)
            }
        }
    }

    // MARK: - Personal Markdown Notes
    private var notesSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                sectionHeader(title: "PERSONAL NOTES", icon: "square.and.pencil")
                Spacer()
                Text("Auto-saved")
                    .font(.appCaptionSmall)
                    .foregroundStyle(Color.textMuted)
            }

            TextEditor(text: $notesText)
                .font(.appBodyRegular)
                .foregroundStyle(Color.textPrimary)
                .scrollContentBackground(.hidden)
                .frame(minHeight: 100)
                .padding(12)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color.obsidianCard)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color.obsidianCardBorder, lineWidth: 1)
                )
                .onChange(of: notesText) { _, newValue in
                    onSaveNotes(newValue)
                }
        }
    }

    // MARK: - Tags Section
    private var tagsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionHeader(title: "TAGS", icon: "number")

            // Current Tags
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(reel.tags, id: \.self) { tag in
                        HStack(spacing: 4) {
                            Text("#\(tag)")
                                .font(.appCaptionPill)
                                .foregroundStyle(Color.cyanAccent)

                            Button {
                                onRemoveTag(tag)
                            } label: {
                                Image(systemName: "xmark")
                                    .font(.system(size: 9, weight: .bold))
                                    .foregroundStyle(Color.textMuted)
                            }
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Capsule().fill(Color.cyanAccent.opacity(0.12)))
                        .overlay(Capsule().stroke(Color.cyanAccent.opacity(0.3), lineWidth: 1))
                    }
                }
            }

            // Add Tag Field
            HStack {
                TextField("Add custom tag...", text: $newTagInput)
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textPrimary)
                    .onSubmit {
                        submitNewTag()
                    }

                Button {
                    submitNewTag()
                } label: {
                    Text("Add")
                        .font(.appCaptionPill)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color.cyanAccent)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.obsidianPill))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.obsidianPillBorder, lineWidth: 1))
        }
    }

    private func submitNewTag() {
        let clean = newTagInput.trimmingCharacters(in: .whitespacesAndNewlines)
        if !clean.isEmpty {
            onAddTag(clean)
            newTagInput = ""
        }
    }

    // MARK: - Action Buttons Section
    private var actionButtonsSection: some View {
        HStack(spacing: 12) {
            // Reprocess
            Button {
                onReprocess()
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "arrow.triangle.2.circlepath")
                        .font(.system(size: 13, weight: .semibold))
                    Text("Reprocess")
                        .font(.appBodyRegular)
                        .fontWeight(.semibold)
                }
                .foregroundStyle(Color.cyanAccent)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(RoundedRectangle(cornerRadius: 14).fill(Color.obsidianCard))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.cyanAccent.opacity(0.3), lineWidth: 1))
            }
            .buttonStyle(.plain)

            // Delete
            Button {
                isShowingDeleteConfirmation = true
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "trash")
                        .font(.system(size: 13, weight: .semibold))
                    Text("Delete")
                        .font(.appBodyRegular)
                        .fontWeight(.semibold)
                }
                .foregroundStyle(Color.statusError)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(RoundedRectangle(cornerRadius: 14).fill(Color.obsidianCard))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.statusError.opacity(0.3), lineWidth: 1))
            }
            .buttonStyle(.plain)
        }
    }

    private func sectionHeader(title: String, icon: String) -> some View {
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
}
