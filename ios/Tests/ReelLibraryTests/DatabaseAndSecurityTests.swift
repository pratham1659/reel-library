//
//  DatabaseAndSecurityTests.swift
//  ReelLibraryTests
//

import XCTest
import GRDB
@testable import ReelLibrary

final class DatabaseAndSecurityTests: XCTestCase {
    var inMemoryDatabase: AppDatabase!
    var reelDao: ReelDao!
    var tokenUsageDao: TokenUsageDao!
    var keychainVault: KeychainVault!

    override func setUpWithError() throws {
        try super.setUpWithError()
        inMemoryDatabase = try AppDatabase.makeInMemory()
        reelDao = inMemoryDatabase.reelDao
        tokenUsageDao = inMemoryDatabase.tokenUsageDao
        keychainVault = KeychainVault(service: "com.reellibrary.test.\(UUID().uuidString)")
    }

    override func tearDownWithError() throws {
        keychainVault.clearAll()
        keychainVault = nil
        reelDao = nil
        tokenUsageDao = nil
        inMemoryDatabase = nil
        try super.tearDownWithError()
    }

    // MARK: - ReelRecord CRUD Tests

    func testReelRecordInsertionAndRetrieval() throws {
        var reel = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/C123456789/",
            shortcode: "C123456789",
            creatorHandle: "@tech_innovator",
            caption: "Swift 6 Concurrency Deep Dive",
            title: "Mastering Swift 6",
            oneLineSummary: "Explains how actor isolation and Sendable types guarantee data-race safety.",
            takeawaysJson: "[]",
            tagsJson: "[]",
            engineUsed: "Cloud (Groq LPU)",
            status: .queued
        )
        reel.takeaways = ["Use actors for mutable state", "Enable strict concurrency checking"]
        reel.tags = ["swift", "ios", "concurrency"]

        let insertedId = try reelDao.insert(&reel)
        XCTAssertGreaterThan(insertedId, 0)
        XCTAssertEqual(reel.id, insertedId)

        let fetched = try reelDao.getById(insertedId)
        XCTAssertNotNil(fetched)
        XCTAssertEqual(fetched?.canonicalUrl, "https://www.instagram.com/reel/C123456789/")
        XCTAssertEqual(fetched?.shortcode, "C123456789")
        XCTAssertEqual(fetched?.creatorHandle, "@tech_innovator")
        XCTAssertEqual(fetched?.status, .queued)
        XCTAssertEqual(fetched?.takeaways.count, 2)
        XCTAssertEqual(fetched?.takeaways.first, "Use actors for mutable state")
        XCTAssertEqual(fetched?.tags.count, 3)
        XCTAssertTrue(fetched?.tags.contains("concurrency") == true)
    }

    func testReelStatusAndNotesUpdate() throws {
        var reel = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/C987654321/",
            shortcode: "C987654321",
            status: .queued
        )
        let insertedId = try reelDao.insert(&reel)

        // 1. Update status to EXTRACTING
        try reelDao.updateStatus(id: insertedId, status: .extracting)
        var fetched = try reelDao.getById(insertedId)
        XCTAssertEqual(fetched?.status, .extracting)

        // 2. Update status to COMPLETED
        try reelDao.updateStatus(id: insertedId, status: .completed)
        fetched = try reelDao.getById(insertedId)
        XCTAssertEqual(fetched?.status, .completed)
        XCTAssertTrue(fetched?.status.isTerminal == true)

        // 3. Update notes
        try reelDao.updateNotes(id: insertedId, notes: "Remember to share this with the team")
        fetched = try reelDao.getById(insertedId)
        XCTAssertEqual(fetched?.userNotes, "Remember to share this with the team")
    }

    // MARK: - SQLite FTS5 Full-Text Search Tests

    func testSQLiteFts5FullTextSearch() throws {
        let reel1 = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/AAA111/",
            shortcode: "AAA111",
            creatorHandle: "@ai_researcher",
            caption: "Local LLMs on mobile devices",
            transcript: "WhisperKit enables real-time on-device audio transcription with low latency.",
            title: "WhisperKit and On-Device CoreML",
            oneLineSummary: "Running quantized models on Apple Neural Engine",
            tagsJson: "[\"coreml\", \"ai\", \"whisper\"]"
        )
        let reel2 = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/BBB222/",
            shortcode: "BBB222",
            creatorHandle: "@chef_culinary",
            caption: "Authentic Neapolitan Pizza in 90 seconds",
            transcript: "Ferment the dough for 48 hours for best flavor and digestibility.",
            title: "Perfect Sourdough Pizza Dough",
            oneLineSummary: "Hydration ratios and high-heat oven baking techniques.",
            tagsJson: "[\"cooking\", \"pizza\", \"food\"]"
        )
        _ = try reelDao.insert(reel: reel1)
        _ = try reelDao.insert(reel: reel2)

        // Search for "WhisperKit"
        let aiResults = try reelDao.searchFts(query: "WhisperKit")
        XCTAssertEqual(aiResults.count, 1)
        XCTAssertEqual(aiResults.first?.shortcode, "AAA111")

        // Search for "pizza"
        let foodResults = try reelDao.searchFts(query: "pizza")
        XCTAssertEqual(foodResults.count, 1)
        XCTAssertEqual(foodResults.first?.shortcode, "BBB222")

        // Search for "neural" (prefix or stem match)
        let neuralResults = try reelDao.searchFts(query: "neural")
        XCTAssertEqual(neuralResults.count, 1)
        XCTAssertEqual(neuralResults.first?.shortcode, "AAA111")
    }

    // MARK: - TokenUsageDao Tests

    func testTokenUsageInsertionAndAggregation() throws {
        let log1 = TokenUsageRecord(
            timestamp: 1700000000000,
            dateString: "2026-10-31",
            monthString: "2026-10",
            engineType: "CLOUD",
            provider: "Groq LPU (Whisper)",
            promptTokens: 100,
            completionTokens: 250,
            totalTokens: 350,
            latencyMs: 320,
            statusCode: "200 OK"
        )
        let log2 = TokenUsageRecord(
            timestamp: 1700000010000,
            dateString: "2026-10-31",
            monthString: "2026-10",
            engineType: "CLOUD",
            provider: "Groq LPU (Qwen)",
            promptTokens: 500,
            completionTokens: 300,
            totalTokens: 800,
            latencyMs: 410,
            statusCode: "200 OK"
        )
        try tokenUsageDao.insert(record: log1)
        try tokenUsageDao.insert(record: log2)

        let daily = try tokenUsageDao.getDayWiseConsumption(engineType: "CLOUD")
        XCTAssertEqual(daily.count, 1)
        XCTAssertEqual(daily.first?.period, "2026-10-31")
        XCTAssertEqual(daily.first?.totalTokens, 1150)
        XCTAssertEqual(daily.first?.promptTokens, 600)
        XCTAssertEqual(daily.first?.completionTokens, 550)
        XCTAssertEqual(daily.first?.requestCount, 2)

        let monthly = try tokenUsageDao.getMonthWiseConsumption(engineType: "CLOUD")
        XCTAssertEqual(monthly.count, 1)
        XCTAssertEqual(monthly.first?.period, "2026-10")
        XCTAssertEqual(monthly.first?.totalTokens, 1150)
    }

    // MARK: - KeychainVault Security Tests

    func testKeychainVaultReadWriteDelete() {
        let testKey = "test_groq_api_key"
        let testValue = "gsk_test_1234567890abcdef"

        // 1. Save
        let saved = keychainVault.save(key: testKey, value: testValue)
        XCTAssertTrue(saved)

        // 2. Read
        let readValue = keychainVault.read(key: testKey)
        XCTAssertEqual(readValue, testValue)

        // 3. Update with new value
        let updatedValue = "gsk_test_updated_value_999"
        keychainVault.save(key: testKey, value: updatedValue)
        XCTAssertEqual(keychainVault.read(key: testKey), updatedValue)

        // 4. Delete
        let deleted = keychainVault.delete(key: testKey)
        XCTAssertTrue(deleted)
        XCTAssertNil(keychainVault.read(key: testKey))
    }

    func testKeychainVaultGroqKeyConvenienceMethods() {
        let testKey = "gsk_production_live_key"
        keychainVault.setGroqApiKey(testKey)
        XCTAssertEqual(keychainVault.getGroqApiKey(), testKey)

        keychainVault.deleteGroqApiKey()
        XCTAssertEqual(keychainVault.getGroqApiKey(), "")
    }

    func testKeychainVaultMultiProviderKeys() {
        let openAIKey = "sk-proj-test12345678"
        let claudeKey = "sk-ant-api03-testkey987"
        let customKey = "sk-or-v1-customkey333"

        keychainVault.setOpenAIApiKey(openAIKey)
        keychainVault.setClaudeApiKey(claudeKey)
        keychainVault.setCustomApiKey(customKey)

        XCTAssertEqual(keychainVault.getOpenAIApiKey(), openAIKey)
        XCTAssertEqual(keychainVault.getClaudeApiKey(), claudeKey)
        XCTAssertEqual(keychainVault.getCustomApiKey(), customKey)

        XCTAssertEqual(keychainVault.apiKey(for: .openAI), openAIKey)
        XCTAssertEqual(keychainVault.apiKey(for: .claude), claudeKey)
        XCTAssertEqual(keychainVault.apiKey(for: .custom), customKey)

        // Delete individually
        keychainVault.deleteApiKey(for: .openAI)
        XCTAssertEqual(keychainVault.getOpenAIApiKey(), "")
        XCTAssertEqual(keychainVault.getClaudeApiKey(), claudeKey)

        keychainVault.deleteApiKey(for: .claude)
        keychainVault.deleteApiKey(for: .custom)
        XCTAssertEqual(keychainVault.getClaudeApiKey(), "")
        XCTAssertEqual(keychainVault.getCustomApiKey(), "")
    }

    func testPreferencesRepositoryMultiProvider() {
        let testDefaults = UserDefaults(suiteName: "test.preferences.\(UUID().uuidString)")!
        let repo = PreferencesRepository(userDefaults: testDefaults, keychainVault: keychainVault)

        repo.setSelectedProvider(.claude)
        repo.setClaudeApiKey("sk-ant-test1234")
        repo.setOpenAIApiKey("sk-openai-test5678")
        repo.setCustomSettings(baseUrl: "https://api.deepseek.com/v1", summarizerModel: "deepseek-chat", transcriptionModel: "whisper-1")

        let prefs = repo.getPreferences()
        XCTAssertEqual(prefs.selectedProvider, .claude)
        XCTAssertEqual(prefs.claudeApiKey, "sk-ant-test1234")
        XCTAssertEqual(prefs.openAIApiKey, "sk-openai-test5678")
        XCTAssertEqual(prefs.customBaseUrl, "https://api.deepseek.com/v1")
        XCTAssertEqual(prefs.customSummarizerModel, "deepseek-chat")
        XCTAssertEqual(prefs.customTranscriptionModel, "whisper-1")
    }

    // MARK: - Filter Models & Dynamic Query Tests

    func testFilterAndSortReels() throws {
        let r1 = ReelRecord(
            canonicalUrl: "https://instagram.com/reel/1",
            shortcode: "1",
            creatorHandle: "@alice",
            caption: "Alpha Reel",
            title: "Alpha",
            tagsJson: "[\"ios\"]",
            status: .completed,
            createdAt: 1000
        )
        let r2 = ReelRecord(
            canonicalUrl: "https://instagram.com/reel/2",
            shortcode: "2",
            creatorHandle: "@bob",
            caption: "Beta Reel",
            title: "Beta",
            tagsJson: "[\"swift\"]",
            status: .queued,
            createdAt: 2000
        )
        try reelDao.insert(reel: r1)
        try reelDao.insert(reel: r2)

        // Filter by status .completed
        let completed = try reelDao.filterReels(status: .completed)
        XCTAssertEqual(completed.count, 1)
        XCTAssertEqual(completed.first?.shortcode, "1")

        // Filter by tag "swift"
        let swiftReels = try reelDao.filterReels(tag: "swift")
        XCTAssertEqual(swiftReels.count, 1)
        XCTAssertEqual(swiftReels.first?.shortcode, "2")

        // Sort by Title ascending
        let sorted = try reelDao.filterReels(orderBy: .title, sortDirection: .ascending)
        XCTAssertEqual(sorted.count, 2)
        XCTAssertEqual(sorted.first?.title, "Alpha")
        XCTAssertEqual(sorted.last?.title, "Beta")
    }
}
