//
//  StatusBadge.swift
//  ReelLibrary
//

import SwiftUI

public struct StatusBadge: View {
    public let status: IngestionStatus
    public var compact: Bool = false

    @State private var isPulsing = false

    public init(status: IngestionStatus, compact: Bool = false) {
        self.status = status
        self.compact = compact
    }

    private var statusColor: Color {
        switch status {
        case .queued:
            return Color(hex: 0x94A3B8)
        case .extracting:
            return Color.cyanAccent
        case .transcribing:
            return Color(hex: 0xC084FC)
        case .summarizing:
            return Color.sparkleOrange
        case .completed:
            return Color.statusReady
        case .failed:
            return Color.statusError
        }
    }

    public var body: some View {
        HStack(spacing: compact ? 3 : 5) {
            Image(systemName: status.sfSymbolName)
                .font(.system(size: compact ? 9 : 11, weight: .bold))
                .foregroundStyle(statusColor)
                .rotationEffect(.degrees(status.isInProgress && isPulsing ? 360 : 0))
                .animation(
                    status.isInProgress
                        ? .linear(duration: 2.0).repeatForever(autoreverses: false)
                        : .default,
                    value: isPulsing
                )

            if !compact {
                Text(status.displayName)
                    .font(.appCaptionSmall)
                    .fontWeight(.semibold)
                    .foregroundStyle(statusColor)
            }
        }
        .padding(.horizontal, compact ? 6 : 9)
        .padding(.vertical, compact ? 3 : 5)
        .background(
            Capsule()
                .fill(statusColor.opacity(0.12))
        )
        .overlay(
            Capsule()
                .stroke(statusColor.opacity(0.35), lineWidth: 1)
        )
        .onAppear {
            if status.isInProgress {
                isPulsing = true
            }
        }
    }
}
