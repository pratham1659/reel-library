//
//  ReelRecord.swift
//  ReelLibrary
//

import Foundation
import GRDB

/// Primary persistent record representing an indexed Instagram Reel.
public struct ReelRecord: Identifiable, Codable, Sendable, Equatable {
    public var id: Int64?
    public var canonicalUrl: String
    public var shortcode: String
    public var creatorHandle: String?
    public var caption: String?
    public var audioPath: String?
    public var thumbnailUrl: String?
    public var transcript: String?
    public var title: String?
    public var oneLineSummary: String?
    public var takeawaysJson: String
    public var tagsJson: String
    public var engineUsed: String?
    public var status: IngestionStatus
    public var errorMessage: String?
    public var transcriptNote: String?
    public var userNotes: String?
    public var createdAt: Int64

    public init(
        id: Int64? = nil,
        canonicalUrl: String,
        shortcode: String,
        creatorHandle: String? = nil,
        caption: String? = nil,
        audioPath: String? = nil,
        thumbnailUrl: String? = nil,
        transcript: String? = nil,
        title: String? = nil,
        oneLineSummary: String? = nil,
        takeawaysJson: String = "[]",
        tagsJson: String = "[]",
        engineUsed: String? = nil,
        status: IngestionStatus = .queued,
        errorMessage: String? = nil,
        transcriptNote: String? = nil,
        userNotes: String? = nil,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.canonicalUrl = canonicalUrl
        self.shortcode = shortcode
        self.creatorHandle = creatorHandle
        self.caption = caption
        self.audioPath = audioPath
        self.thumbnailUrl = thumbnailUrl
        self.transcript = transcript
        self.title = title
        self.oneLineSummary = oneLineSummary
        self.takeawaysJson = takeawaysJson
        self.tagsJson = tagsJson
        self.engineUsed = engineUsed
        self.status = status
        self.errorMessage = errorMessage
        self.transcriptNote = transcriptNote
        self.userNotes = userNotes
        self.createdAt = createdAt
    }

    /// Convenience access for parsed takeaways list.
    public var takeaways: [String] {
        get {
            guard let data = takeawaysJson.data(using: .utf8),
                  let items = try? JSONDecoder().decode([String].self, from: data) else {
                return []
            }
            return items
        }
        set {
            if let data = try? JSONEncoder().encode(newValue),
               let json = String(data: data, encoding: .utf8) {
                takeawaysJson = json
            } else {
                takeawaysJson = "[]"
            }
        }
    }

    /// Convenience access for parsed tags list.
    public var tags: [String] {
        get {
            guard let data = tagsJson.data(using: .utf8),
                  let items = try? JSONDecoder().decode([String].self, from: data) else {
                return []
            }
            return items
        }
        set {
            if let data = try? JSONEncoder().encode(newValue),
               let json = String(data: data, encoding: .utf8) {
                tagsJson = json
            } else {
                tagsJson = "[]"
            }
        }
    }

    /// Date representation of `createdAt` epoch milliseconds.
    public var createdDate: Date {
        Date(timeIntervalSince1970: TimeInterval(createdAt) / 1000.0)
    }

    /// User-friendly display title with fallbacks.
    public var displayTitle: String {
        if let t = title, !t.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return t
        }
        if let c = caption, !c.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return String(c.prefix(60))
        }
        return "Instagram Reel #\(shortcode)"
    }

    /// User-friendly creator handle with leading @ symbol.
    public var displayCreator: String {
        if let h = creatorHandle, !h.isEmpty {
            return h.hasPrefix("@") ? h : "@\(h)"
        }
        return "@creator"
    }

    /// Abbreviated relative date string (e.g. "2h ago", "3d ago").
    public var formattedDate: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: createdDate, relativeTo: Date())
    }
}

// MARK: - GRDB Record Conformance
extension ReelRecord: FetchableRecord, MutablePersistableRecord {
    public static let databaseTableName = "reels"

    public enum Columns: String, ColumnExpression {
        case id
        case canonicalUrl
        case shortcode
        case creatorHandle
        case caption
        case audioPath
        case thumbnailUrl
        case transcript
        case title
        case oneLineSummary
        case takeawaysJson
        case tagsJson
        case engineUsed
        case status
        case errorMessage
        case transcriptNote
        case userNotes
        case createdAt
    }

    public mutating func didInsert(_ inserted: InsertionSuccess) {
        id = inserted.rowID
    }
}

// MARK: - Universal Import / Export Models
public struct ReelLibraryExport: Codable, Sendable {
    public let version: Int
    public let exportDate: String
    public let exportTimestamp: Int64
    public let totalReels: Int
    public let reels: [ReelRecord]

    public init(
        version: Int = 1,
        exportDate: String,
        exportTimestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        totalReels: Int,
        reels: [ReelRecord]
    ) {
        self.version = version
        self.exportDate = exportDate
        self.exportTimestamp = exportTimestamp
        self.totalReels = totalReels
        self.reels = reels
    }
}

public struct ReelSummary: Codable, Sendable {
    public let title: String
    public let oneLineSummary: String
    public let actionableTakeaways: [String]
    public let tags: [String]

    enum CodingKeys: String, CodingKey {
        case title
        case oneLineSummary = "one_line_summary"
        case actionableTakeaways = "actionable_takeaways"
        case tags
    }

    public init(
        title: String,
        oneLineSummary: String,
        actionableTakeaways: [String],
        tags: [String]
    ) {
        self.title = title
        self.oneLineSummary = oneLineSummary
        self.actionableTakeaways = actionableTakeaways
        self.tags = tags
    }
}
