//
//  TokenUsageDao.swift
//  ReelLibrary
//

import Foundation
import GRDB

/// Data Access Object providing persistent tracking and aggregation for AI token telemetry.
public struct TokenUsageDao: Sendable {
    private let dbQueue: DatabaseQueue

    public init(dbQueue: DatabaseQueue) {
        self.dbQueue = dbQueue
    }

    // MARK: - Inserts

    @discardableResult
    public func insert(_ record: inout TokenUsageRecord) throws -> Int64 {
        try dbQueue.write { db in
            try record.insert(db)
            return record.id!
        }
    }

    @discardableResult
    public func insert(record: TokenUsageRecord) throws -> TokenUsageRecord {
        var copy = record
        try dbQueue.write { db in
            try copy.insert(db)
        }
        return copy
    }

    // MARK: - Aggregations

    /// Retrieves daily token consumption aggregated by date.
    public func getDayWiseConsumption(engineType: String) throws -> [TokenPeriodAggregate] {
        try dbQueue.read { db in
            let sql = """
                SELECT 
                    dateString AS period,
                    SUM(totalTokens) AS totalTokens,
                    SUM(promptTokens) AS promptTokens,
                    SUM(completionTokens) AS completionTokens,
                    COUNT(*) AS requestCount,
                    CAST(AVG(latencyMs) AS INTEGER) AS avgLatencyMs
                FROM token_usage
                WHERE engineType = ?
                GROUP BY dateString
                ORDER BY dateString DESC
                """
            return try TokenPeriodAggregate.fetchAll(db, sql: sql, arguments: [engineType])
        }
    }

    /// Retrieves monthly token consumption aggregated by month.
    public func getMonthWiseConsumption(engineType: String) throws -> [TokenPeriodAggregate] {
        try dbQueue.read { db in
            let sql = """
                SELECT 
                    monthString AS period,
                    SUM(totalTokens) AS totalTokens,
                    SUM(promptTokens) AS promptTokens,
                    SUM(completionTokens) AS completionTokens,
                    COUNT(*) AS requestCount,
                    CAST(AVG(latencyMs) AS INTEGER) AS avgLatencyMs
                FROM token_usage
                WHERE engineType = ?
                GROUP BY monthString
                ORDER BY monthString DESC
                """
            return try TokenPeriodAggregate.fetchAll(db, sql: sql, arguments: [engineType])
        }
    }

    /// Fetches the latest N token usage audit entries.
    public func getRecentLogs(engineType: String, limit: Int = 50) throws -> [TokenUsageRecord] {
        try dbQueue.read { db in
            let sql = """
                SELECT * FROM token_usage
                WHERE engineType = ?
                ORDER BY timestamp DESC
                LIMIT ?
                """
            return try TokenUsageRecord.fetchAll(db, sql: sql, arguments: [engineType, limit])
        }
    }

    /// Deletes all token usage log records.
    public func clearAll() throws {
        try dbQueue.write { db in
            try db.execute(sql: "DELETE FROM token_usage")
        }
    }

    /// Backfills legacy whisper records where input/output tokens were zero.
    public func backfillWhisperLogsInputOutput() throws {
        try dbQueue.write { db in
            let sql = """
                UPDATE token_usage
                SET promptTokens = CASE WHEN latencyMs > 0 THEN CAST((latencyMs / 40) AS INTEGER) ELSE 24 END,
                    completionTokens = CASE WHEN latencyMs > 0 THEN CAST((latencyMs / 4) AS INTEGER) ELSE 72 END,
                    totalTokens = CASE WHEN latencyMs > 0 THEN CAST((latencyMs / 40) AS INTEGER) ELSE 24 END
                WHERE (provider LIKE '%Whisper%' OR payloadSnippet LIKE '%whisper%') AND promptTokens = 0
                """
            try db.execute(sql: sql)
        }
    }
}
