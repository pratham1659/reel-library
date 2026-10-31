//
//  SortFilterSheet.swift
//  ReelLibrary
//

import SwiftUI

public struct SortFilterSheet: View {
    @Binding public var orderBy: OrderBy
    @Binding public var sortDirection: SortDirection
    @Binding public var selectedStatus: IngestionStatus?
    @Binding public var selectedTag: String?
    public var availableTags: [String]
    public var onReset: () -> Void
    public var onDismiss: () -> Void

    public init(
        orderBy: Binding<OrderBy>,
        sortDirection: Binding<SortDirection>,
        selectedStatus: Binding<IngestionStatus?>,
        selectedTag: Binding<String?>,
        availableTags: [String] = [],
        onReset: @escaping () -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self._orderBy = orderBy
        self._sortDirection = sortDirection
        self._selectedStatus = selectedStatus
        self._selectedTag = selectedTag
        self.availableTags = availableTags
        self.onReset = onReset
        self.onDismiss = onDismiss
    }

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    // 1. Order By Section
                    sectionHeader(title: "ORDER BY", icon: "arrow.up.arrow.down")
                    HStack(spacing: 10) {
                        ForEach(OrderBy.allCases, id: \.self) { option in
                            selectableOptionButton(
                                title: option.displayName,
                                isSelected: orderBy == option
                            ) {
                                orderBy = option
                            }
                        }
                    }

                    // 2. Sort Direction Section
                    sectionHeader(title: "DIRECTION", icon: "arrow.triangle.swap")
                    HStack(spacing: 10) {
                        ForEach(SortDirection.allCases, id: \.self) { direction in
                            selectableOptionButton(
                                title: direction.displayName,
                                isSelected: sortDirection == direction
                            ) {
                                sortDirection = direction
                            }
                        }
                    }

                    // 3. Status Filter Section
                    sectionHeader(title: "STATUS FILTER", icon: "line.3.horizontal.decrease.circle")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            statusFilterChip(label: "All", isSelected: selectedStatus == nil) {
                                selectedStatus = nil
                            }

                            ForEach(IngestionStatus.allCases, id: \.self) { status in
                                statusFilterChip(
                                    label: status.displayName,
                                    isSelected: selectedStatus == status
                                ) {
                                    selectedStatus = (selectedStatus == status) ? nil : status
                                }
                            }
                        }
                        .padding(.vertical, 2)
                    }

                    // 4. Tags Filter Section
                    if !availableTags.isEmpty {
                        sectionHeader(title: "TAGS", icon: "number")
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 90), spacing: 8)], spacing: 8) {
                            ForEach(availableTags, id: \.self) { tag in
                                let clean = tag.hasPrefix("#") ? tag : "#\(tag)"
                                Button {
                                    if selectedTag == tag {
                                        selectedTag = nil
                                    } else {
                                        selectedTag = tag
                                    }
                                } label: {
                                    Text(clean)
                                        .font(.appCaptionPill)
                                        .foregroundStyle(selectedTag == tag ? Color.black : Color.cyanAccent)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 7)
                                        .frame(maxWidth: .infinity)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(selectedTag == tag ? Color.cyanAccent : Color.obsidianPill)
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(selectedTag == tag ? Color.cyanAccent : Color.obsidianPillBorder, lineWidth: 1)
                                        )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    Spacer(minLength: 20)
                }
                .padding(20)
            }
            .background(Color.obsidianBg.ignoresSafeArea())
            .navigationTitle("Sort & Filter")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Reset") {
                        onReset()
                    }
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.sparkleOrange)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        onDismiss()
                    }
                    .font(.appBodyLead)
                    .fontWeight(.semibold)
                    .foregroundStyle(Color.activeBlue)
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }

    private func sectionHeader(title: String, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(Color.sparkleOrange)
            Text(title)
                .font(.appCaptionSmall)
                .fontWeight(.bold)
                .foregroundStyle(Color.textMuted)
        }
    }

    private func selectableOptionButton(
        title: String,
        isSelected: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Text(title)
                .font(.appBodyRegular)
                .fontWeight(isSelected ? .semibold : .regular)
                .foregroundStyle(isSelected ? Color.textPrimary : Color.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(isSelected ? Color.activeBlue.opacity(0.2) : Color.obsidianCard)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isSelected ? Color.activeBlue : Color.obsidianCardBorder, lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }

    private func statusFilterChip(
        label: String,
        isSelected: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Text(label)
                .font(.appCaptionPill)
                .fontWeight(isSelected ? .bold : .medium)
                .foregroundStyle(isSelected ? Color.black : Color.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(isSelected ? Color.sparkleOrange : Color.obsidianPill)
                )
                .overlay(
                    Capsule()
                        .stroke(isSelected ? Color.sparkleOrange : Color.obsidianPillBorder, lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}
