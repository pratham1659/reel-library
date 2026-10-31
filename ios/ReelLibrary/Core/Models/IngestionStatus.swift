//
//  IngestionStatus.swift
//  ReelLibrary
//

import Foundation
import GRDB

/// State machine tracking the background ingestion lifecycle of an Instagram Reel.
public enum IngestionStatus: String, Codable, CaseIterable, Sendable, DatabaseValueConvertible {
    case queued = "QUEUED"
    case extracting = "EXTRACTING"
    case transcribing = "TRANSCRIBING"
    case summarizing = "SUMMARIZING"
    case completed = "COMPLETED"
    case failed = "FAILED"

    /// User-friendly display label.
    public var displayName: String {
        switch self {
        case .queued:
            return "Queued"
        case .extracting:
            return "Extracting Audio"
        case .transcribing:
            return "Transcribing"
        case .summarizing:
            return "Summarizing"
        case .completed:
            return "Completed"
        case .failed:
            return "Failed"
        }
    }

    /// SF Symbol icon associated with the stage.
    public var sfSymbolName: String {
        switch self {
        case .queued:
            return "clock.arrow.circlepath"
        case .extracting:
            return "waveform.and.magnifyingglass"
        case .transcribing:
            return "captions.bubble"
        case .summarizing:
            return "brain.head.profile"
        case .completed:
            return "checkmark.circle.fill"
        case .failed:
            return "exclamationmark.triangle.fill"
        }
    }

    /// Whether this status represents a final terminal state.
    public var isTerminal: Bool {
        self == .completed || self == .failed
    }

    /// Whether this status represents an active processing pipeline.
    public var isInProgress: Bool {
        self == .extracting || self == .transcribing || self == .summarizing
    }
}
