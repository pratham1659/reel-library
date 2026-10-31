//
//  ReelDao.swift
//  ReelLibrary
//

import Foundation
import GRDB

/// Data Access Object providing CRUD, SQLite FTS5 search, and dynamic filtering for Reels.
public struct ReelDao: Sendable {
    private let dbQueue: DatabaseQueue

    public init(dbQueue: DatabaseQueue) {
        self.dbQueue = dbQueue
    }

    // MARK: - Insert & Upsert

    @discardableResult
    public func insert(_ reel: inout ReelRecord) throws -> Int64 {
        try dbQueue.write { db in
            try reel.insert(db)
            return reel.id!
        }
    }

    @discardableResult
    public func insert(reel: ReelRecord) throws -> ReelRecord {
        var copy = reel
        try dbQueue.write { db in
            try copy.insert(db)
        }
        return copy
    }

    @discardableResult
    public func insertAll(_ reels: [ReelRecord]) throws -> [Int64] {
        try dbQueue.write { db in
            var ids: [Int64] = []
            for var item in reels {
                try item.insert(db)
                if let id = item.id {
                    ids.append(id)
                }
            }
            return ids
        }
    }

    public func update(_ reel: ReelRecord) throws {
        try dbQueue.write { db in
            try reel.update(db)
        }
    }

    // MARK: - Query Single

    public func getById(_ id: Int64) throws -> ReelRecord? {
        try dbQueue.read { db in
            try ReelRecord.fetchOne(db, key: id)
        }
    }

    public func findByCanonicalUrl(_ canonicalUrl: String) throws -> ReelRecord? {
        try dbQueue.read { db in
            try ReelRecord
                .filter(ReelRecord.Columns.canonicalUrl == canonicalUrl)
                .fetchOne(db)
        }
    }

    public func findByShortcode(_ shortcode: String) throws -> ReelRecord? {
        try dbQueue.read { db in
            try ReelRecord
                .filter(ReelRecord.Columns.shortcode == shortcode)
                .fetchOne(db)
        }
    }

    // MARK: - Query Multiple

    public func getAll() throws -> [ReelRecord] {
        try dbQueue.read { db in
            try ReelRecord
                .order(ReelRecord.Columns.createdAt.desc)
                .fetchAll(db)
        }
    }

    public func getAll(orderBy: OrderBy, sortDirection: SortDirection) throws -> [ReelRecord] {
        try filterReels(orderBy: orderBy, sortDirection: sortDirection)
    }

    public func count() throws -> Int {
        try dbQueue.read { db in
            try ReelRecord.fetchCount(db)
        }
    }

    // MARK: - Full-Text Search (SQLite FTS5)

    /// Executes full-text search across title, summary, transcript, caption, and tags.
    public func searchFts(query: String) throws -> [ReelRecord] {
        let sanitized = sanitizeFtsQuery(query)
        guard !sanitized.isEmpty else {
            return try getAll()
        }

        return try dbQueue.read { db in
            let sql = """
                SELECT reels.* FROM reels
                JOIN reels_fts ON reels.id = reels_fts.rowid
                WHERE reels_fts MATCH ?
                ORDER BY bm25(reels_fts) ASC, reels.createdAt DESC
                """
            return try ReelRecord.fetchAll(db, sql: sql, arguments: [sanitized])
        }
    }

    // MARK: - Dynamic Filter & Sort

    public func filterReels(
        query: String? = nil,
        tag: String? = nil,
        creator: String? = nil,
        status: IngestionStatus? = nil,
        orderBy: OrderBy = .date,
        sortDirection: SortDirection = .descending
    ) throws -> [ReelRecord] {
        let sanitizedQuery = query.flatMap { sanitizeFtsQuery($0) }?.trimmingCharacters(in: .whitespacesAndNewlines)
        let hasQuery = sanitizedQuery != nil && !sanitizedQuery!.isEmpty

        return try dbQueue.read { db in
            var sql: String
            var arguments: [DatabaseValueConvertible] = []

            if hasQuery {
                sql = """
                    SELECT reels.* FROM reels
                    JOIN reels_fts ON reels.id = reels_fts.rowid
                    WHERE reels_fts MATCH ?
                    """
                arguments.append(sanitizedQuery!)
            } else {
                sql = "SELECT reels.* FROM reels WHERE 1=1"
            }

            if let tag = tag?.trimmingCharacters(in: .whitespacesAndNewlines), !tag.isEmpty {
                sql += " AND reels.tagsJson LIKE ?"
                arguments.append("%\"\(tag)\"%")
            }

            if let creator = creator?.trimmingCharacters(in: .whitespacesAndNewlines), !creator.isEmpty {
                sql += " AND (reels.creatorHandle = ? OR reels.creatorHandle LIKE ?)"
                arguments.append(creator)
                arguments.append("%\(creator)%")
            }

            if let status = status {
                sql += " AND reels.status = ?"
                arguments.append(status.rawValue)
            }

            // ORDER BY clause
            let dir = (sortDirection == .ascending) ? "ASC" : "DESC"
            switch orderBy {
            case .date:
                sql += " ORDER BY reels.createdAt \(dir)"
            case .title:
                sql += " ORDER BY LOWER(COALESCE(reels.title, reels.caption, '')) \(dir), reels.createdAt DESC"
            case .creator:
                sql += " ORDER BY LOWER(COALESCE(reels.creatorHandle, '')) \(dir), reels.createdAt DESC"
            }

            return try ReelRecord.fetchAll(db, sql: sql, arguments: StatementArguments(arguments))
        }
    }

    // MARK: - Updates

    public func updateStatus(id: Int64, status: IngestionStatus, errorMessage: String? = nil) throws {
        try dbQueue.write { db in
            try db.execute(
                sql: "UPDATE reels SET status = ?, errorMessage = ? WHERE id = ?",
                arguments: [status.rawValue, errorMessage, id]
            )
        }
    }

    public func updateNotes(id: Int64, notes: String?) throws {
        try dbQueue.write { db in
            try db.execute(
                sql: "UPDATE reels SET userNotes = ? WHERE id = ?",
                arguments: [notes, id]
            )
        }
    }

    public func updateTags(id: Int64, tagsJson: String) throws {
        try dbQueue.write { db in
            try db.execute(
                sql: "UPDATE reels SET tagsJson = ? WHERE id = ?",
                arguments: [tagsJson, id]
            )
        }
    }

    public func updateTags(id: Int64, tags: [String]) throws {
        let json: String
        if let data = try? JSONEncoder().encode(tags),
           let str = String(data: data, encoding: .utf8) {
            json = str
        } else {
            json = "[]"
        }
        try updateTags(id: id, tagsJson: json)
    }

    // MARK: - Deletions

    public func delete(id: Int64) throws {
        _ = try dbQueue.write { db in
            try ReelRecord.deleteOne(db, key: id)
        }
    }

    public func delete(ids: [Int64]) throws {
        guard !ids.isEmpty else { return }
        _ = try dbQueue.write { db in
            try ReelRecord.deleteAll(db, keys: ids)
        }
    }

    public func deleteAll() throws {
        _ = try dbQueue.write { db in
            try ReelRecord.deleteAll(db)
        }
    }

    // MARK: - Observation

    /// Returns an observation tracking all reels ordered by creation date.
    public func observeAllReels() -> ValueObservation<ValueReducers.Fetch<[ReelRecord]>> {
        ValueObservation.tracking { db in
            try ReelRecord.order(ReelRecord.Columns.createdAt.desc).fetchAll(db)
        }
    }

    /// Emits an AsyncStream of reels as changes occur in the database.
    public func observeReels() -> AsyncStream<[ReelRecord]> {
        AsyncStream { continuation in
            final class CancellableBox: @unchecked Sendable {
                var cancellable: AnyDatabaseCancellable?
            }
            let box = CancellableBox()
            box.cancellable = observeAllReels().start(
                in: dbQueue,
                onError: { _ in continuation.finish() },
                onChange: { reels in continuation.yield(reels) }
            )

            continuation.onTermination = { _ in
                box.cancellable?.cancel()
            }
        }
    }

    // MARK: - Helpers

    /// Formats search text safely for SQLite FTS5 prefix queries.
    private func sanitizeFtsQuery(_ query: String) -> String {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return "" }

        let tokens = trimmed.components(separatedBy: CharacterSet.alphanumerics.inverted)
            .filter { !$0.isEmpty }

        guard !tokens.isEmpty else { return "" }
        return tokens.map { "\"\($0)\"*" }.joined(separator: " ")
    }
}
