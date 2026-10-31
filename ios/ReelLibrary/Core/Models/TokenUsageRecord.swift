//
//  TokenUsageRecord.swift
//  ReelLibrary
//

import Foundation
import GRDB

/// Audit record tracking individual AI inference token consumption, provider latency, and status.
public struct TokenUsageRecord: Identifiable, Codable, Sendable, Equatable {
    public var id: Int64?
    public var timestamp: Int64
    public var dateString: String      // e.g. "2026-10-31"
    public var monthString: String     // e.g. "2026-10"
    public var engineType: String      // "CLOUD" or "LOCAL"
    public var provider: String        // "Groq LPU (Whisper)", "Groq LPU (Qwen)", "WhisperKit Local", etc.
    public var promptTokens: Int64
    public var completionTokens: Int64
    public var totalTokens: Int64
    public var latencyMs: Int64
    public var statusCode: String      // e.g. "200 OK"
    public var errorDetails: String?
    public var payloadSnippet: String?

    public init(
        id: Int64? = nil,
        timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        dateString: String,
        monthString: String,
        engineType: String,
        provider: String,
        promptTokens: Int64 = 0,
        completionTokens: Int64 = 0,
        totalTokens: Int64 = 0,
        latencyMs: Int64 = 0,
        statusCode: String = "200 OK",
        errorDetails: String? = nil,
        payloadSnippet: String? = nil
    ) {
        self.id = id
        self.timestamp = timestamp
        self.dateString = dateString
        self.monthString = monthString
        self.engineType = engineType
        self.provider = provider
        self.promptTokens = promptTokens
        self.completionTokens = completionTokens
        self.totalTokens = totalTokens
        self.latencyMs = latencyMs
        self.statusCode = statusCode
        self.errorDetails = errorDetails
        self.payloadSnippet = payloadSnippet
    }
}

// MARK: - GRDB Conformance
extension TokenUsageRecord: FetchableRecord, MutablePersistableRecord {
    public static let databaseTableName = "token_usage"

    public enum Columns: String, ColumnExpression {
        case id
        case timestamp
        case dateString
        case monthString
        case engineType
        case provider
        case promptTokens
        case completionTokens
        case totalTokens
        case latencyMs
        case statusCode
        case errorDetails
        case payloadSnippet
    }

    public mutating func didInsert(_ inserted: InsertionSuccess) {
        id = inserted.rowID
    }
}

/// Aggregated token usage for a given time period (e.g. daily or monthly).
public struct TokenPeriodAggregate: Identifiable, Codable, Sendable, FetchableRecord, Equatable {
    public var id: String { period }
    public let period: String
    public let totalTokens: Int64
    public let promptTokens: Int64
    public let completionTokens: Int64
    public let requestCount: Int
    public let avgLatencyMs: Int64

    public init(
        period: String,
        totalTokens: Int64,
        promptTokens: Int64,
        completionTokens: Int64,
        requestCount: Int,
        avgLatencyMs: Int64
    ) {
        self.period = period
        self.totalTokens = totalTokens
        self.promptTokens = promptTokens
        self.completionTokens = completionTokens
        self.requestCount = requestCount
        self.avgLatencyMs = avgLatencyMs
    }
}
