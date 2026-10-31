//
//  PreferencesRepository.swift
//  ReelLibrary
//

import Foundation
import Combine
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app", category: "PreferencesRepository")

/// Repository managing user preferences, Secure Enclave Keychain storage,
/// and real-time AI token consumption telemetry.
public final class PreferencesRepository: @unchecked Sendable {
    public static let shared = PreferencesRepository()

    private let userDefaults: UserDefaults
    private let keychainVault: KeychainVault
    private let tokenUsageDao: TokenUsageDao?

    private let lock = NSLock()
    private let preferencesSubject: CurrentValueSubject<AppPreferences, Never>

    public var preferencesPublisher: AnyPublisher<AppPreferences, Never> {
        preferencesSubject.eraseToAnyPublisher()
    }

    public var preferencesStream: AsyncStream<AppPreferences> {
        AsyncStream { continuation in
            final class CancellableBox: @unchecked Sendable {
                var cancellable: AnyCancellable?
            }
            let box = CancellableBox()
            box.cancellable = preferencesSubject.sink { prefs in
                continuation.yield(prefs)
            }
            continuation.onTermination = { _ in
                box.cancellable?.cancel()
            }
        }
    }

    public var currentPreferences: AppPreferences {
        lock.lock()
        defer { lock.unlock() }
        return preferencesSubject.value
    }

    public init(
        userDefaults: UserDefaults = UserDefaults(suiteName: "group.com.reellibrary.vault") ?? .standard,
        keychainVault: KeychainVault = .shared,
        tokenUsageDao: TokenUsageDao? = nil
    ) {
        self.userDefaults = userDefaults
        self.keychainVault = keychainVault
        self.tokenUsageDao = tokenUsageDao

        let initial = Self.loadPreferences(from: userDefaults, keychain: keychainVault)
        self.preferencesSubject = CurrentValueSubject(initial)
    }

    // MARK: - Getters & Setters

    public func getPreferences() -> AppPreferences {
        currentPreferences
    }

    public func setEngineMode(_ mode: EngineMode) {
        mutatePreferences { prefs in
            prefs.engineMode = mode
            userDefaults.set(mode.rawValue, forKey: Keys.engineMode)
        }
    }

    public func setSelectedProvider(_ provider: AIProvider) {
        mutatePreferences { prefs in
            prefs.selectedProvider = provider
            userDefaults.set(provider.rawValue, forKey: Keys.selectedProvider)
        }
    }

    public func setGroqApiKey(_ key: String) {
        let trimmed = key.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            deleteGroqApiKey()
        } else {
            keychainVault.setGroqApiKey(trimmed)
            mutatePreferences { prefs in
                prefs.groqApiKey = trimmed
            }
        }
    }

    public func deleteGroqApiKey() {
        keychainVault.deleteGroqApiKey()
        mutatePreferences { prefs in
            prefs.groqApiKey = ""
        }
    }

    public func setOpenAIApiKey(_ key: String) {
        let trimmed = key.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            deleteOpenAIApiKey()
        } else {
            keychainVault.setOpenAIApiKey(trimmed)
            mutatePreferences { prefs in
                prefs.openAIApiKey = trimmed
            }
        }
    }

    public func deleteOpenAIApiKey() {
        keychainVault.deleteOpenAIApiKey()
        mutatePreferences { prefs in
            prefs.openAIApiKey = ""
        }
    }

    public func setClaudeApiKey(_ key: String) {
        let trimmed = key.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            deleteClaudeApiKey()
        } else {
            keychainVault.setClaudeApiKey(trimmed)
            mutatePreferences { prefs in
                prefs.claudeApiKey = trimmed
            }
        }
    }

    public func deleteClaudeApiKey() {
        keychainVault.deleteClaudeApiKey()
        mutatePreferences { prefs in
            prefs.claudeApiKey = ""
        }
    }

    public func setCustomApiKey(_ key: String) {
        let trimmed = key.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            deleteCustomApiKey()
        } else {
            keychainVault.setCustomApiKey(trimmed)
            mutatePreferences { prefs in
                prefs.customApiKey = trimmed
            }
        }
    }

    public func deleteCustomApiKey() {
        keychainVault.deleteCustomApiKey()
        mutatePreferences { prefs in
            prefs.customApiKey = ""
        }
    }

    public func setCustomSettings(
        baseUrl: String,
        summarizerModel: String,
        transcriptionModel: String
    ) {
        mutatePreferences { prefs in
            prefs.customBaseUrl = baseUrl
            prefs.customSummarizerModel = summarizerModel
            prefs.customTranscriptionModel = transcriptionModel
            userDefaults.set(baseUrl, forKey: Keys.customBaseUrl)
            userDefaults.set(summarizerModel, forKey: Keys.customSummarizerModel)
            userDefaults.set(transcriptionModel, forKey: Keys.customTranscriptionModel)
        }
    }

    public func setLocalModelStatus(
        isWhisper: Bool,
        isDownloaded: Bool,
        path: String? = nil
    ) {
        mutatePreferences { prefs in
            if isWhisper {
                prefs.isWhisperModelDownloaded = isDownloaded
                userDefaults.set(isDownloaded, forKey: Keys.isWhisperDownloaded)
                if let path {
                    prefs.whisperModelPath = path
                    userDefaults.set(path, forKey: Keys.whisperPath)
                }
            } else {
                prefs.isLlmModelDownloaded = isDownloaded
                userDefaults.set(isDownloaded, forKey: Keys.isLlmDownloaded)
                if let path {
                    prefs.llmModelPath = path
                    userDefaults.set(path, forKey: Keys.llmPath)
                }
            }
        }
    }

    public func setShowTagsOnCards(_ show: Bool) {
        mutatePreferences { prefs in
            prefs.showTagsOnCards = show
            userDefaults.set(show, forKey: Keys.showTagsOnCards)
        }
    }

    public func setCardClickAction(_ action: String) {
        mutatePreferences { prefs in
            prefs.cardClickAction = action
            userDefaults.set(action, forKey: Keys.cardClickAction)
        }
    }

    public func setAutoSummarization(_ enabled: Bool) {
        mutatePreferences { prefs in
            prefs.autoSummarization = enabled
            userDefaults.set(enabled, forKey: Keys.autoSummarization)
        }
    }

    public func setSummaryStyle(_ style: String) {
        mutatePreferences { prefs in
            prefs.summaryStyle = style
            userDefaults.set(style, forKey: Keys.summaryStyle)
        }
    }

    public func setAutoTagging(_ enabled: Bool) {
        mutatePreferences { prefs in
            prefs.autoTagging = enabled
            userDefaults.set(enabled, forKey: Keys.autoTagging)
        }
    }

    public func setThemeMode(_ mode: String) {
        mutatePreferences { prefs in
            prefs.themeMode = mode
            userDefaults.set(mode, forKey: Keys.themeMode)
        }
    }

    public func setLoginState(isLoggedIn: Bool, name: String, email: String, initial: String) {
        mutatePreferences { prefs in
            prefs.isLoggedIn = isLoggedIn
            prefs.userName = name
            prefs.userEmail = email
            prefs.userAvatarInitial = initial

            userDefaults.set(isLoggedIn, forKey: Keys.isLoggedIn)
            userDefaults.set(name, forKey: Keys.userName)
            userDefaults.set(email, forKey: Keys.userEmail)
            userDefaults.set(initial, forKey: Keys.userAvatarInitial)
        }
    }

    public func setCloudBackupEnabled(_ enabled: Bool) {
        mutatePreferences { prefs in
            prefs.isCloudBackupEnabled = enabled
            userDefaults.set(enabled, forKey: Keys.isCloudBackupEnabled)
        }
    }

    public func setBackupOnlyOnWifi(_ onlyWifi: Bool) {
        mutatePreferences { prefs in
            prefs.backupOnlyOnWifi = onlyWifi
            userDefaults.set(onlyWifi, forKey: Keys.backupOnlyOnWifi)
        }
    }

    public func updateLastBackupTime(_ timeStr: String) {
        mutatePreferences { prefs in
            prefs.lastBackupTimeFormatted = timeStr
            userDefaults.set(timeStr, forKey: Keys.lastBackupTime)
        }
    }

    // MARK: - Daily Reset Check

    private func checkAndPerformDailyResetIfNeeded() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: Date())
        let midnightCutoff = Int64(startOfDay.timeIntervalSince1970 * 1000)

        let lastReset = userDefaults.object(forKey: Keys.lastDailyResetTime) as? Int64 ?? 0
        if lastReset < midnightCutoff {
            mutatePreferences { prefs in
                prefs.totalTokensUsed = 0
                prefs.totalPromptTokens = 0
                prefs.totalCompletionTokens = 0
                prefs.groqTotalTokens = 0
                prefs.groqPromptTokens = 0
                prefs.groqCompletionTokens = 0
                prefs.groqCallsCount = 0
                prefs.localTotalTokens = 0
                prefs.localPromptTokens = 0
                prefs.localCompletionTokens = 0
                prefs.localGemmaCallsCount = 0
                prefs.localWhisperCallsCount = 0
                prefs.lastDailyResetTime = now

                userDefaults.set(0, forKey: Keys.totalTokensUsed)
                userDefaults.set(0, forKey: Keys.totalPromptTokens)
                userDefaults.set(0, forKey: Keys.totalCompletionTokens)
                userDefaults.set(0, forKey: Keys.groqTotalTokens)
                userDefaults.set(0, forKey: Keys.groqPromptTokens)
                userDefaults.set(0, forKey: Keys.groqCompletionTokens)
                userDefaults.set(0, forKey: Keys.groqCallsCount)
                userDefaults.set(0, forKey: Keys.localTotalTokens)
                userDefaults.set(0, forKey: Keys.localPromptTokens)
                userDefaults.set(0, forKey: Keys.localCompletionTokens)
                userDefaults.set(0, forKey: Keys.localGemmaCallsCount)
                userDefaults.set(0, forKey: Keys.localWhisperCallsCount)
                userDefaults.set(now, forKey: Keys.lastDailyResetTime)
            }
        }
    }

    // MARK: - Telemetry Tracking

    public func recordTokenUsage(
        promptTokens: Int64,
        completionTokens: Int64,
        latencyMs: Int64 = 0,
        statusCode: String = "200 OK",
        provider: String = "",
        model: String = "",
        errorMessage: String? = nil
    ) async throws {
        checkAndPerformDailyResetIfNeeded()

        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let dateFormatter = DateFormatter()
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let dateString = dateFormatter.string(from: Date())

        dateFormatter.dateFormat = "yyyy-MM"
        let monthString = dateFormatter.string(from: Date())

        let totalForCall = promptTokens + completionTokens
        let isLocal = provider.localizedCaseInsensitiveContains("WhisperKit") ||
            provider.localizedCaseInsensitiveContains("Local") ||
            provider.localizedCaseInsensitiveContains("On-Device")
        let engineType = isLocal ? "LOCAL" : "CLOUD"

        // 1. Persist audit log record to SQLite via DAO if available
        if let dao = tokenUsageDao ?? AppDatabase.shared.tokenUsageDao as TokenUsageDao? {
            let record = TokenUsageRecord(
                timestamp: now,
                dateString: dateString,
                monthString: monthString,
                engineType: engineType,
                provider: provider,
                promptTokens: promptTokens,
                completionTokens: completionTokens,
                totalTokens: totalForCall,
                latencyMs: latencyMs,
                statusCode: statusCode,
                errorDetails: errorMessage,
                payloadSnippet: model
            )
            _ = try? dao.insert(record: record)
        }

        // 2. Update live in-memory & UserDefaults telemetry
        mutatePreferences { prefs in
            prefs.totalTokensUsed += totalForCall
            prefs.totalPromptTokens += promptTokens
            prefs.totalCompletionTokens += completionTokens
            prefs.lastCallTokens = totalForCall
            prefs.lastCallPromptTokens = promptTokens
            prefs.lastCallCompletionTokens = completionTokens
            prefs.lastCallLatencyMs = latencyMs
            prefs.lastCallStatusCode = statusCode
            prefs.lastCallProvider = provider

            userDefaults.set(prefs.totalTokensUsed, forKey: Keys.totalTokensUsed)
            userDefaults.set(prefs.totalPromptTokens, forKey: Keys.totalPromptTokens)
            userDefaults.set(prefs.totalCompletionTokens, forKey: Keys.totalCompletionTokens)
            userDefaults.set(prefs.lastCallTokens, forKey: Keys.lastCallTokens)
            userDefaults.set(prefs.lastCallPromptTokens, forKey: Keys.lastCallPromptTokens)
            userDefaults.set(prefs.lastCallCompletionTokens, forKey: Keys.lastCallCompletionTokens)
            userDefaults.set(prefs.lastCallLatencyMs, forKey: Keys.lastCallLatencyMs)
            userDefaults.set(prefs.lastCallStatusCode, forKey: Keys.lastCallStatusCode)
            userDefaults.set(prefs.lastCallProvider, forKey: Keys.lastCallProvider)

            if isLocal {
                prefs.localTotalTokens += totalForCall
                prefs.localPromptTokens += promptTokens
                prefs.localCompletionTokens += completionTokens
                if provider.localizedCaseInsensitiveContains("Whisper") {
                    prefs.localWhisperCallsCount += 1
                    userDefaults.set(prefs.localWhisperCallsCount, forKey: Keys.localWhisperCallsCount)
                } else {
                    prefs.localGemmaCallsCount += 1
                    userDefaults.set(prefs.localGemmaCallsCount, forKey: Keys.localGemmaCallsCount)
                }
                userDefaults.set(prefs.localTotalTokens, forKey: Keys.localTotalTokens)
                userDefaults.set(prefs.localPromptTokens, forKey: Keys.localPromptTokens)
                userDefaults.set(prefs.localCompletionTokens, forKey: Keys.localCompletionTokens)
            } else {
                prefs.groqTotalTokens += totalForCall
                prefs.groqPromptTokens += promptTokens
                prefs.groqCompletionTokens += completionTokens
                prefs.groqCallsCount += 1

                userDefaults.set(prefs.groqTotalTokens, forKey: Keys.groqTotalTokens)
                userDefaults.set(prefs.groqPromptTokens, forKey: Keys.groqPromptTokens)
                userDefaults.set(prefs.groqCompletionTokens, forKey: Keys.groqCompletionTokens)
                userDefaults.set(prefs.groqCallsCount, forKey: Keys.groqCallsCount)
            }
        }
    }

    public func resetTokenStats() {
        mutatePreferences { prefs in
            prefs.totalTokensUsed = 0
            prefs.totalPromptTokens = 0
            prefs.totalCompletionTokens = 0
            prefs.groqTotalTokens = 0
            prefs.groqPromptTokens = 0
            prefs.groqCompletionTokens = 0
            prefs.groqCallsCount = 0
            prefs.localTotalTokens = 0
            prefs.localPromptTokens = 0
            prefs.localCompletionTokens = 0
            prefs.localGemmaCallsCount = 0
            prefs.localWhisperCallsCount = 0

            userDefaults.set(0, forKey: Keys.totalTokensUsed)
            userDefaults.set(0, forKey: Keys.totalPromptTokens)
            userDefaults.set(0, forKey: Keys.totalCompletionTokens)
            userDefaults.set(0, forKey: Keys.groqTotalTokens)
            userDefaults.set(0, forKey: Keys.groqPromptTokens)
            userDefaults.set(0, forKey: Keys.groqCompletionTokens)
            userDefaults.set(0, forKey: Keys.groqCallsCount)
            userDefaults.set(0, forKey: Keys.localTotalTokens)
            userDefaults.set(0, forKey: Keys.localPromptTokens)
            userDefaults.set(0, forKey: Keys.localCompletionTokens)
            userDefaults.set(0, forKey: Keys.localGemmaCallsCount)
            userDefaults.set(0, forKey: Keys.localWhisperCallsCount)
        }
    }

    // MARK: - Private Helpers

    private func mutatePreferences(_ block: (inout AppPreferences) -> Void) {
        lock.lock()
        var updated = preferencesSubject.value
        block(&updated)
        preferencesSubject.value = updated
        lock.unlock()
    }

    private static func loadPreferences(from defaults: UserDefaults, keychain: KeychainVault) -> AppPreferences {
        let engineString = defaults.string(forKey: Keys.engineMode) ?? EngineMode.cloudOnly.rawValue
        let engineMode = EngineMode(rawValue: engineString) ?? .cloudOnly

        let providerString = defaults.string(forKey: Keys.selectedProvider) ?? AIProvider.groq.rawValue
        let selectedProvider = AIProvider(rawValue: providerString) ?? .groq

        let groqApiKey = keychain.getGroqApiKey()
        let openAIApiKey = keychain.getOpenAIApiKey()
        let claudeApiKey = keychain.getClaudeApiKey()
        let customApiKey = keychain.getCustomApiKey()

        let customBaseUrl = defaults.string(forKey: Keys.customBaseUrl) ?? "https://openrouter.ai/api/v1"
        let customSummarizerModel = defaults.string(forKey: Keys.customSummarizerModel) ?? "deepseek/deepseek-chat"
        let customTranscriptionModel = defaults.string(forKey: Keys.customTranscriptionModel) ?? "whisper-1"

        return AppPreferences(
            engineMode: engineMode,
            selectedProvider: selectedProvider,
            groqApiKey: groqApiKey,
            openAIApiKey: openAIApiKey,
            claudeApiKey: claudeApiKey,
            customApiKey: customApiKey,
            customBaseUrl: customBaseUrl,
            customSummarizerModel: customSummarizerModel,
            customTranscriptionModel: customTranscriptionModel,
            isWhisperModelDownloaded: defaults.bool(forKey: Keys.isWhisperDownloaded),
            isLlmModelDownloaded: defaults.bool(forKey: Keys.isLlmDownloaded),
            whisperModelPath: defaults.string(forKey: Keys.whisperPath),
            llmModelPath: defaults.string(forKey: Keys.llmPath),
            showTagsOnCards: defaults.object(forKey: Keys.showTagsOnCards) as? Bool ?? true,
            cardClickAction: defaults.string(forKey: Keys.cardClickAction) ?? "READER",
            autoSummarization: defaults.object(forKey: Keys.autoSummarization) as? Bool ?? true,
            summaryStyle: defaults.string(forKey: Keys.summaryStyle) ?? "CONCISE",
            autoTagging: defaults.object(forKey: Keys.autoTagging) as? Bool ?? true,
            themeMode: defaults.string(forKey: Keys.themeMode) ?? "OBSIDIAN_DARK",
            isLoggedIn: defaults.bool(forKey: Keys.isLoggedIn),
            userName: defaults.string(forKey: Keys.userName) ?? "Not logged in",
            userEmail: defaults.string(forKey: Keys.userEmail) ?? "",
            userAvatarInitial: defaults.string(forKey: Keys.userAvatarInitial) ?? "?",
            isCloudBackupEnabled: defaults.bool(forKey: Keys.isCloudBackupEnabled),
            backupOnlyOnWifi: defaults.object(forKey: Keys.backupOnlyOnWifi) as? Bool ?? true,
            cloudBackupFolder: defaults.string(forKey: Keys.cloudBackupFolder) ?? "ReelLibrary_Vault",
            lastBackupTimeFormatted: defaults.string(forKey: Keys.lastBackupTime) ?? "Never backed up",
            totalTokensUsed: defaults.object(forKey: Keys.totalTokensUsed) as? Int64 ?? 0,
            totalPromptTokens: defaults.object(forKey: Keys.totalPromptTokens) as? Int64 ?? 0,
            totalCompletionTokens: defaults.object(forKey: Keys.totalCompletionTokens) as? Int64 ?? 0,
            lastCallTokens: defaults.object(forKey: Keys.lastCallTokens) as? Int64 ?? 0,
            lastCallPromptTokens: defaults.object(forKey: Keys.lastCallPromptTokens) as? Int64 ?? 0,
            lastCallCompletionTokens: defaults.object(forKey: Keys.lastCallCompletionTokens) as? Int64 ?? 0,
            lastCallLatencyMs: defaults.object(forKey: Keys.lastCallLatencyMs) as? Int64 ?? 0,
            lastCallStatusCode: defaults.string(forKey: Keys.lastCallStatusCode) ?? "",
            lastCallProvider: defaults.string(forKey: Keys.lastCallProvider) ?? "",
            groqTotalTokens: defaults.object(forKey: Keys.groqTotalTokens) as? Int64 ?? 0,
            groqPromptTokens: defaults.object(forKey: Keys.groqPromptTokens) as? Int64 ?? 0,
            groqCompletionTokens: defaults.object(forKey: Keys.groqCompletionTokens) as? Int64 ?? 0,
            groqCallsCount: defaults.object(forKey: Keys.groqCallsCount) as? Int64 ?? 0,
            localTotalTokens: defaults.object(forKey: Keys.localTotalTokens) as? Int64 ?? 0,
            localPromptTokens: defaults.object(forKey: Keys.localPromptTokens) as? Int64 ?? 0,
            localCompletionTokens: defaults.object(forKey: Keys.localCompletionTokens) as? Int64 ?? 0,
            localGemmaCallsCount: defaults.object(forKey: Keys.localGemmaCallsCount) as? Int64 ?? 0,
            localWhisperCallsCount: defaults.object(forKey: Keys.localWhisperCallsCount) as? Int64 ?? 0,
            lastDailyResetTime: defaults.object(forKey: Keys.lastDailyResetTime) as? Int64 ?? 0
        )
    }

    private enum Keys {
        static let engineMode = "engine_mode"
        static let selectedProvider = "selected_provider"
        static let customBaseUrl = "custom_base_url"
        static let customSummarizerModel = "custom_summarizer_model"
        static let customTranscriptionModel = "custom_transcription_model"
        static let isWhisperDownloaded = "is_whisper_downloaded"
        static let isLlmDownloaded = "is_llm_downloaded"
        static let whisperPath = "whisper_path"
        static let llmPath = "llm_path"
        static let showTagsOnCards = "show_tags_on_cards"
        static let cardClickAction = "card_click_action"
        static let autoSummarization = "auto_summarization"
        static let summaryStyle = "summary_style"
        static let autoTagging = "auto_tagging"
        static let themeMode = "theme_mode"
        static let isLoggedIn = "is_logged_in"
        static let userName = "user_name"
        static let userEmail = "user_email"
        static let userAvatarInitial = "user_avatar_initial"
        static let isCloudBackupEnabled = "is_cloud_backup_enabled"
        static let backupOnlyOnWifi = "backup_only_on_wifi"
        static let cloudBackupFolder = "cloud_backup_folder"
        static let lastBackupTime = "last_backup_time"

        static let totalTokensUsed = "total_tokens_used"
        static let totalPromptTokens = "total_prompt_tokens"
        static let totalCompletionTokens = "total_completion_tokens"
        static let lastCallTokens = "last_call_tokens"
        static let lastCallPromptTokens = "last_call_prompt_tokens"
        static let lastCallCompletionTokens = "last_call_completion_tokens"
        static let lastCallLatencyMs = "last_call_latency_ms"
        static let lastCallStatusCode = "last_call_status_code"
        static let lastCallProvider = "last_call_provider"
        static let lastDailyResetTime = "last_daily_reset_time"

        static let groqTotalTokens = "groq_total_tokens"
        static let groqPromptTokens = "groq_prompt_tokens"
        static let groqCompletionTokens = "groq_completion_tokens"
        static let groqCallsCount = "groq_calls_count"

        static let localTotalTokens = "local_total_tokens"
        static let localPromptTokens = "local_prompt_tokens"
        static let localCompletionTokens = "local_completion_tokens"
        static let localGemmaCallsCount = "local_gemma_calls_count"
        static let localWhisperCallsCount = "local_whisper_calls_count"
    }
}
