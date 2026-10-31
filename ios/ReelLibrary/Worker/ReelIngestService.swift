import Foundation
import UserNotifications
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app", category: "ReelIngestService")

public actor ReelIngestService: Sendable {
    public static let shared = ReelIngestService()

    private let database: AppDatabase
    private let preferencesRepo: PreferencesRepository
    private let dispatcher: DualEngineDispatcher
    private let extractor: InstagramExtractor
    private let audioExtractor: AudioExtractor

    public init(
        database: AppDatabase = .shared,
        preferencesRepo: PreferencesRepository = .shared,
        dispatcher: DualEngineDispatcher = DualEngineDispatcher(),
        extractor: InstagramExtractor = InstagramExtractor(),
        audioExtractor: AudioExtractor = AudioExtractor()
    ) {
        self.database = database
        self.preferencesRepo = preferencesRepo
        self.dispatcher = dispatcher
        self.extractor = extractor
        self.audioExtractor = audioExtractor
    }

    /// Primary entry point coordinating background ingestion for a queued reel.
    /// - Parameters:
    ///   - reelId: The primary key of the record in the SQLite database.
    ///   - canonicalUrl: The normalized Instagram Reel URL.
    public func ingest(reelId: Int64, canonicalUrl: String) async throws {
        let dao = database.reelDao

        do {
            // Step 1: Update status to EXTRACTING
            try dao.updateStatus(id: reelId, status: .extracting)
            logger.info("Ingestion started for reel #\(reelId) - EXTRACTING")

            // Step 2: Scrape Instagram metadata & optionally download video
            let metadata = try await extractor.extract(canonicalUrl: canonicalUrl, downloadVideo: true)

            // Step 3: Extract compact M4A audio if video was downloaded
            var audioFileUrl: URL?
            var audioDurationSeconds: Double = 0.0

            if let videoFile = metadata.downloadedVideoFile, FileManager.default.fileExists(atPath: videoFile.path) {
                defer {
                    // Clean up downloaded raw video file to conserve disk space
                    try? FileManager.default.removeItem(at: videoFile)
                }

                if let audioResult = try? await audioExtractor.extractAudio(from: videoFile) {
                    audioFileUrl = audioResult.audioUrl
                    audioDurationSeconds = audioResult.durationSeconds
                }
            }

            // Step 4: Update status to TRANSCRIBING
            try dao.updateStatus(id: reelId, status: .transcribing)
            logger.info("Reel #\(reelId) status -> TRANSCRIBING")

            let prefs = preferencesRepo.currentPreferences
            let transcriptionEngine = dispatcher.resolveTranscriptionEngine(prefs: prefs)

            var transcript: String = "Not available"
            if let audioUrl = audioFileUrl, FileManager.default.fileExists(atPath: audioUrl.path) {
                if let rawTranscript = try? await transcriptionEngine.transcribe(audioFile: audioUrl) {
                    let trimmed = rawTranscript.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !trimmed.isEmpty {
                        transcript = trimmed
                    }
                }
            }

            // Step 5: Check Duration Guard
            // If reel audio duration > 60 seconds and mode is Cloud:
            // Extract metadata + transcript, set title from caption, mark as COMPLETED
            // with AI summary deferred until requested on-demand (saving Groq API rate limits).
            let isCloudMode = prefs.engineMode == .cloudOnly
            let shouldSkipAiForDuration = audioDurationSeconds > 60.0 && isCloudMode

            guard var existing = try dao.getById(reelId) else {
                throw NSError(
                    domain: "ReelIngestService",
                    code: 404,
                    userInfo: [NSLocalizedDescriptionKey: "Reel record #\(reelId) was not found in database."]
                )
            }

            let cleanHandle = metadata.creatorHandle
            let cleanCaption = metadata.caption

            if shouldSkipAiForDuration {
                let fallbackTitle = Self.generateFallbackTitle(from: cleanCaption, creatorHandle: cleanHandle)

                existing.creatorHandle = cleanHandle
                existing.caption = cleanCaption
                existing.audioPath = audioFileUrl?.path
                existing.thumbnailUrl = metadata.thumbnailUrl ?? existing.thumbnailUrl
                existing.transcript = transcript
                existing.title = fallbackTitle
                existing.oneLineSummary = nil
                existing.takeawaysJson = "[]"
                existing.tagsJson = "[]"
                existing.engineUsed = transcriptionEngine.name
                existing.status = .completed
                existing.errorMessage = nil

                try dao.update(existing)
                logger.info("Reel #\(reelId) duration > 60s in Cloud mode: saved with deferred AI summary.")

                let notifTitle = String(cleanCaption.prefix(40))
                postCompletionNotification(
                    title: notifTitle.isEmpty ? "Reel Indexed" : notifTitle,
                    body: "Reel is over 1 min. Tap in app to run AI summary on demand."
                )
                return
            }

            // Step 6: Update status to SUMMARIZING for reels <= 60 seconds (or local mode)
            try dao.updateStatus(id: reelId, status: .summarizing)
            logger.info("Reel #\(reelId) status -> SUMMARIZING")

            let summarizationEngine = dispatcher.resolveSummarizationEngine(prefs: prefs)
            let summaryTranscript = (transcript == "Not available") ? "" : transcript
            let summary = try await summarizationEngine.summarize(caption: metadata.caption, transcript: summaryTranscript)

            // Step 7: Persist finalized entity in SQLite DB
            existing.creatorHandle = cleanHandle
            existing.caption = cleanCaption
            existing.audioPath = audioFileUrl?.path
            existing.thumbnailUrl = metadata.thumbnailUrl ?? existing.thumbnailUrl
            existing.transcript = transcript
            existing.title = summary.title
            existing.oneLineSummary = summary.oneLineSummary
            existing.takeaways = summary.actionableTakeaways
            existing.tags = summary.tags
            existing.engineUsed = "\(transcriptionEngine.name) + \(summarizationEngine.name)"
            existing.status = .completed
            existing.errorMessage = nil

            try dao.update(existing)
            logger.info("Reel #\(reelId) completed successfully: '\(summary.title)'")

            postCompletionNotification(
                title: summary.title,
                body: summary.oneLineSummary
            )
        } catch {
            logger.error("Reel #\(reelId) ingestion failed: \(error.localizedDescription)")
            try? dao.updateStatus(id: reelId, status: .failed, errorMessage: error.localizedDescription)
            throw error
        }
    }

    /// On-demand summarization for reels whose AI summary was deferred by the 60-second duration guard.
    public func summarizeOnDemand(reelId: Int64) async throws -> ReelRecord {
        let dao = database.reelDao
        guard var reel = try dao.getById(reelId) else {
            throw NSError(
                domain: "ReelIngestService",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "Reel #\(reelId) not found."]
            )
        }

        try dao.updateStatus(id: reelId, status: .summarizing)

        let prefs = preferencesRepo.currentPreferences
        let summarizationEngine = dispatcher.resolveSummarizationEngine(prefs: prefs)

        let cleanTranscript = (reel.transcript == "Not available") ? "" : (reel.transcript ?? "")
        let summary = try await summarizationEngine.summarize(caption: reel.caption, transcript: cleanTranscript)

        reel.title = summary.title
        reel.oneLineSummary = summary.oneLineSummary
        reel.takeaways = summary.actionableTakeaways
        reel.tags = summary.tags
        reel.status = .completed
        reel.errorMessage = nil

        try dao.update(reel)
        return reel
    }

    // MARK: - Local Notification Helper

    private func postCompletionNotification(title: String, body: String) {
        let center = UNUserNotificationCenter.current()
        let content = UNMutableNotificationContent()
        content.title = "Reel Library: \(title)"
        content.body = body
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 0.2, repeats: false)
        let request = UNNotificationRequest(
            identifier: "reel_ingest_\(UUID().uuidString)",
            content: content,
            trigger: trigger
        )

        center.add(request) { error in
            if let error = error {
                logger.warning("Failed to post completion notification: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Helpers

    public static func generateFallbackTitle(from caption: String?, creatorHandle: String?) -> String {
        guard let caption = caption, !caption.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return "Instagram Reel by \(creatorHandle ?? "@creator")"
        }

        let trimmed = caption.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.count > 60 {
            let prefix = trimmed.prefix(60)
            return "\(prefix)..."
        }
        return trimmed
    }
}
