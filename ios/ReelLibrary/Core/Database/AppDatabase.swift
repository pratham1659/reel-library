//
//  AppDatabase.swift
//  ReelLibrary
//

import Foundation
import GRDB
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app", category: "AppDatabase")

/// SQLite Database manager powered by GRDB.swift with WAL concurrency,
/// App Group shared container support, and SQLite FTS5 full-text indexing.
public final class AppDatabase: Sendable {
    public let dbQueue: DatabaseQueue

    public static let appGroupIdentifier = "group.com.reellibrary.vault"
    public static let databaseFilename = "reel_library.sqlite"

    /// Shared singleton instance pointing to the App Group container or local Application Support.
    public static let shared: AppDatabase = {
        do {
            let url = databaseURL()
            return try AppDatabase(url: url)
        } catch {
            fatalError("Failed to initialize AppDatabase at shared container URL: \(error)")
        }
    }()

    /// Resolves the filesystem URL for the SQLite database.
    /// Checks App Group container first; if unavailable, falls back to Application Support.
    public static func databaseURL() -> URL {
        let fileManager = FileManager.default
        let folderURL: URL

        if let containerURL = fileManager.containerURL(forSecurityApplicationGroupIdentifier: appGroupIdentifier) {
            folderURL = containerURL
        } else if let appSupportURL = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first {
            folderURL = appSupportURL
        } else {
            folderURL = fileManager.temporaryDirectory
        }

        if !fileManager.fileExists(atPath: folderURL.path) {
            try? fileManager.createDirectory(at: folderURL, withIntermediateDirectories: true)
        }

        return folderURL.appendingPathComponent(databaseFilename)
    }

    /// Creates an in-memory database instance for testing.
    public static func makeInMemory() throws -> AppDatabase {
        var config = Configuration()
        config.busyMode = .timeout(5.0)
        let dbQueue = try DatabaseQueue(configuration: config)
        return try AppDatabase(dbQueue: dbQueue)
    }

    /// Initializes with a file URL.
    public convenience init(url: URL) throws {
        var config = Configuration()
        config.busyMode = .timeout(5.0)
        config.qos = .userInitiated
        config.prepareDatabase { db in
            try db.execute(sql: "PRAGMA journal_mode = WAL;")
        }

        let dbQueue = try DatabaseQueue(path: url.path, configuration: config)
        try self.init(dbQueue: dbQueue)
    }

    /// Initializes with an existing DatabaseQueue and runs pending migrations.
    public init(dbQueue: DatabaseQueue) throws {
        self.dbQueue = dbQueue
        try migrator.migrate(dbQueue)
    }

    // MARK: - DAO Accessors

    public var reelDao: ReelDao {
        ReelDao(dbQueue: dbQueue)
    }

    public var tokenUsageDao: TokenUsageDao {
        TokenUsageDao(dbQueue: dbQueue)
    }

    // MARK: - Migrations

    public var migrator: DatabaseMigrator {
        var migrator = DatabaseMigrator()

        #if DEBUG
        // Speed up migrations during development
        migrator.eraseDatabaseOnSchemaChange = false
        #endif

        migrator.registerMigration("v1_initial_schema") { db in
            // 1. Primary Reels Table
            try db.execute(sql: """
                CREATE TABLE IF NOT EXISTS reels (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    canonicalUrl TEXT NOT NULL UNIQUE,
                    shortcode TEXT NOT NULL,
                    creatorHandle TEXT,
                    caption TEXT,
                    audioPath TEXT,
                    thumbnailUrl TEXT,
                    transcript TEXT,
                    title TEXT,
                    oneLineSummary TEXT,
                    takeawaysJson TEXT NOT NULL DEFAULT '[]',
                    tagsJson TEXT NOT NULL DEFAULT '[]',
                    engineUsed TEXT,
                    status TEXT NOT NULL DEFAULT 'QUEUED',
                    errorMessage TEXT,
                    userNotes TEXT,
                    createdAt INTEGER NOT NULL
                );
                CREATE INDEX IF NOT EXISTS index_reels_createdAt ON reels(createdAt);
                CREATE INDEX IF NOT EXISTS index_reels_status ON reels(status);
                CREATE INDEX IF NOT EXISTS index_reels_shortcode ON reels(shortcode);
                """)

            // 2. Token Usage Audit Table
            try db.execute(sql: """
                CREATE TABLE IF NOT EXISTS token_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp INTEGER NOT NULL,
                    dateString TEXT NOT NULL,
                    monthString TEXT NOT NULL,
                    engineType TEXT NOT NULL,
                    provider TEXT NOT NULL,
                    promptTokens INTEGER NOT NULL,
                    completionTokens INTEGER NOT NULL,
                    totalTokens INTEGER NOT NULL,
                    latencyMs INTEGER NOT NULL,
                    statusCode TEXT NOT NULL,
                    errorDetails TEXT,
                    payloadSnippet TEXT
                );
                CREATE INDEX IF NOT EXISTS index_token_usage_date ON token_usage(dateString);
                CREATE INDEX IF NOT EXISTS index_token_usage_month ON token_usage(monthString);
                CREATE INDEX IF NOT EXISTS index_token_usage_engine ON token_usage(engineType);
                CREATE VIEW IF NOT EXISTS token_usage_logs AS SELECT * FROM token_usage;
                """)

            // 3. SQLite FTS5 Virtual Table with Porter Stemmer & Unicode Tokenizer
            try db.execute(sql: """
                CREATE VIRTUAL TABLE IF NOT EXISTS reels_fts USING fts5(
                    title,
                    oneLineSummary,
                    transcript,
                    caption,
                    tagsJson,
                    content='reels',
                    content_rowid='id',
                    tokenize='porter unicode61'
                );

                -- Triggers to synchronize FTS5 index automatically with primary reels table
                CREATE TRIGGER IF NOT EXISTS reels_ai AFTER INSERT ON reels BEGIN
                    INSERT INTO reels_fts(rowid, title, oneLineSummary, transcript, caption, tagsJson)
                    VALUES (new.id, new.title, new.oneLineSummary, new.transcript, new.caption, new.tagsJson);
                END;

                CREATE TRIGGER IF NOT EXISTS reels_ad AFTER DELETE ON reels BEGIN
                    INSERT INTO reels_fts(reels_fts, rowid, title, oneLineSummary, transcript, caption, tagsJson)
                    VALUES ('delete', old.id, old.title, old.oneLineSummary, old.transcript, old.caption, old.tagsJson);
                END;

                CREATE TRIGGER IF NOT EXISTS reels_au AFTER UPDATE ON reels BEGIN
                    INSERT INTO reels_fts(reels_fts, rowid, title, oneLineSummary, transcript, caption, tagsJson)
                    VALUES ('delete', old.id, old.title, old.oneLineSummary, old.transcript, old.caption, old.tagsJson);
                    INSERT INTO reels_fts(rowid, title, oneLineSummary, transcript, caption, tagsJson)
                    VALUES (new.id, new.title, new.oneLineSummary, new.transcript, new.caption, new.tagsJson);
                END;
                """)
        }

        return migrator
    }
}
