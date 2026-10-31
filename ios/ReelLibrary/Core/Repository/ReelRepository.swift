//
//  ReelRepository.swift
//  ReelLibrary
//

import Foundation
import Combine
import GRDB
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app", category: "ReelRepository")

/// Repository coordinating Reel database operations, full-text searches, and observation pipelines.
public final class ReelRepository: Sendable {
    public static let shared = ReelRepository()

    private let database: AppDatabase
    private var dao: ReelDao {
        database.reelDao
    }

    public init(database: AppDatabase = .shared) {
        self.database = database
    }

    // MARK: - Query Operations

    public func getAllReels() async throws -> [ReelRecord] {
        try dao.getAll()
    }

    public func getReel(id: Int64) async throws -> ReelRecord? {
        try dao.getById(id)
    }

    public func findReel(byCanonicalUrl canonicalUrl: String) async throws -> ReelRecord? {
        try dao.findByCanonicalUrl(canonicalUrl)
    }

    public func findReel(byShortcode shortcode: String) async throws -> ReelRecord? {
        try dao.findByShortcode(shortcode)
    }

    public func count() async throws -> Int {
        try dao.count()
    }

    // MARK: - Search & Filter Operations

    public func searchReels(query: String) async throws -> [ReelRecord] {
        try dao.searchFts(query: query)
    }

    public func filterReels(
        query: String? = nil,
        tag: String? = nil,
        creator: String? = nil,
        status: IngestionStatus? = nil,
        orderBy: OrderBy = .date,
        sortDirection: SortDirection = .descending
    ) async throws -> [ReelRecord] {
        try dao.filterReels(
            query: query,
            tag: tag,
            creator: creator,
            status: status,
            orderBy: orderBy,
            sortDirection: sortDirection
        )
    }

    // MARK: - Mutation Operations

    @discardableResult
    public func saveReel(_ reel: ReelRecord) async throws -> ReelRecord {
        if reel.id != nil {
            try dao.update(reel)
            return reel
        } else {
            return try dao.insert(reel: reel)
        }
    }

    @discardableResult
    public func insertAll(_ reels: [ReelRecord]) async throws -> [Int64] {
        try dao.insertAll(reels)
    }

    public func updateStatus(id: Int64, status: IngestionStatus, errorMessage: String? = nil) async throws {
        try dao.updateStatus(id: id, status: status, errorMessage: errorMessage)
    }

    public func updateNotes(id: Int64, notes: String?) async throws {
        try dao.updateNotes(id: id, notes: notes)
    }

    public func updateTags(id: Int64, tags: [String]) async throws {
        try dao.updateTags(id: id, tags: tags)
    }

    public func deleteReel(id: Int64) async throws {
        try dao.delete(id: id)
    }

    public func deleteReels(ids: [Int64]) async throws {
        try dao.delete(ids: ids)
    }

    public func deleteAll() async throws {
        try dao.deleteAll()
    }

    // MARK: - Observation

    /// Exposes an AsyncStream of reels ordered by latest creation date.
    public func observeReels() -> AsyncStream<[ReelRecord]> {
        dao.observeReels()
    }
}
