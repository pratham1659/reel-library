import Foundation
import Network

public final class NetworkReachabilityMonitor: @unchecked Sendable {
    public static let shared = NetworkReachabilityMonitor()
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.reellibrary.networkreachability")
    private let lock = NSLock()
    private var _isReachable: Bool = true

    public var isReachable: Bool {
        lock.lock()
        defer { lock.unlock() }
        return _isReachable
    }

    private init() {
        monitor.pathUpdateHandler = { [weak self] path in
            guard let self = self else { return }
            self.lock.lock()
            self._isReachable = (path.status == .satisfied)
            self.lock.unlock()
        }
        monitor.start(queue: queue)
    }
}

public final class DualEngineDispatcher: Sendable {
    private let reachabilityMonitor: NetworkReachabilityMonitor
    private let database: AppDatabase

    public init(
        reachabilityMonitor: NetworkReachabilityMonitor = .shared,
        database: AppDatabase = .shared
    ) {
        self.reachabilityMonitor = reachabilityMonitor
        self.database = database
    }

    // MARK: - Telemetry Helpers

    private func recordTokenUsage(
        promptTokens: Int,
        completionTokens: Int,
        latencyMs: Int,
        provider: String,
        model: String = "",
        statusCode: String = "200 OK",
        engineType: String
    ) {
        let now = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        let dateString = dateFormatter.string(from: now)

        let monthFormatter = DateFormatter()
        monthFormatter.dateFormat = "yyyy-MM"
        monthFormatter.locale = Locale(identifier: "en_US_POSIX")
        let monthString = monthFormatter.string(from: now)

        let prompt = Int64(promptTokens)
        let comp = Int64(completionTokens)
        let total = prompt + comp

        let record = TokenUsageRecord(
            timestamp: Int64(now.timeIntervalSince1970 * 1000),
            dateString: dateString,
            monthString: monthString,
            engineType: engineType,
            provider: provider,
            promptTokens: prompt,
            completionTokens: comp,
            totalTokens: total,
            latencyMs: Int64(latencyMs),
            statusCode: statusCode,
            payloadSnippet: model.isEmpty ? nil : model
        )

        _ = try? database.tokenUsageDao.insert(record: record)
    }

    // MARK: - Resolve Transcription Engine

    public func resolveTranscriptionEngine(prefs: AppPreferences) -> any TranscriptionEngine {
        let provider = prefs.selectedProvider

        let onCloudWhisperTelemetry: GroqWhisperEngine.AudioTelemetryCallback = { [weak self] audioSeconds, wordsCount, latencyMs in
            self?.recordTokenUsage(
                promptTokens: Int(audioSeconds),
                completionTokens: wordsCount,
                latencyMs: latencyMs,
                provider: "\(provider.shortName) Whisper",
                model: "whisper",
                engineType: "CLOUD"
            )
        }

        let onLocalWhisperTelemetry: WhisperKitEngine.LocalWhisperTelemetry = { [weak self] prompt, comp, latencyMs, providerName in
            self?.recordTokenUsage(
                promptTokens: prompt,
                completionTokens: comp,
                latencyMs: latencyMs,
                provider: providerName,
                model: "WhisperKit",
                engineType: "LOCAL"
            )
        }

        let customWhisperUrl = prefs.whisperModelPath.flatMap { URL(fileURLWithPath: $0) }
        let whisperFileExists: Bool
        if let targetUrl = customWhisperUrl {
            whisperFileExists = FileManager.default.fileExists(atPath: targetUrl.path)
        } else {
            let defaultModelUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent("WhisperKit/whisper-base")
            whisperFileExists = defaultModelUrl.map { FileManager.default.fileExists(atPath: $0.path) } ?? false
        }

        func makeCloudTranscriptionEngine() -> (any TranscriptionEngine)? {
            switch provider {
            case .groq:
                let key = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return GroqWhisperEngine(apiKey: key, onAudioTranscribed: onCloudWhisperTelemetry)

            case .openAI:
                let key = prefs.openAIApiKey.isEmpty ? KeychainVault.shared.getOpenAIApiKey() : prefs.openAIApiKey
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return OpenAIWhisperEngine(apiKey: key, model: "whisper-1", providerDisplayName: "OpenAI Whisper", onAudioTranscribed: onCloudWhisperTelemetry)

            case .claude:
                // Claude does not provide audio transcription. Seamlessly use OpenAI Whisper, Groq Whisper, or nil.
                let openAIKey = prefs.openAIApiKey.isEmpty ? KeychainVault.shared.getOpenAIApiKey() : prefs.openAIApiKey
                if !openAIKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return OpenAIWhisperEngine(apiKey: openAIKey, model: "whisper-1", providerDisplayName: "OpenAI Whisper (Claude Hybrid)", onAudioTranscribed: onCloudWhisperTelemetry)
                }
                let groqKey = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey
                if !groqKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    return GroqWhisperEngine(apiKey: groqKey, onAudioTranscribed: onCloudWhisperTelemetry)
                }
                return nil

            case .custom:
                let customKey = prefs.customApiKey.isEmpty ? KeychainVault.shared.getCustomApiKey() : prefs.customApiKey
                let model = prefs.customTranscriptionModel.trimmingCharacters(in: .whitespacesAndNewlines)
                if !customKey.isEmpty && !model.isEmpty {
                    return OpenAIWhisperEngine(apiKey: customKey, baseUrl: prefs.customBaseUrl, model: model, providerDisplayName: "Custom Whisper", onAudioTranscribed: onCloudWhisperTelemetry)
                }
                // Fallback to OpenAI or Groq if available
                let openAIKey = prefs.openAIApiKey.isEmpty ? KeychainVault.shared.getOpenAIApiKey() : prefs.openAIApiKey
                if !openAIKey.isEmpty {
                    return OpenAIWhisperEngine(apiKey: openAIKey, onAudioTranscribed: onCloudWhisperTelemetry)
                }
                let groqKey = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey
                if !groqKey.isEmpty {
                    return GroqWhisperEngine(apiKey: groqKey, onAudioTranscribed: onCloudWhisperTelemetry)
                }
                return nil
            }
        }

        switch prefs.engineMode {
        case .cloudOnly:
            if let cloudEngine = makeCloudTranscriptionEngine() {
                return cloudEngine
            } else {
                let keyMissingReason: String
                if provider == .claude {
                    keyMissingReason = "Engine Mode is set to CLOUD_ONLY with Claude: Claude lacks an audio API. Please add an OpenAI or Groq API key in Settings for transcription."
                } else {
                    keyMissingReason = "Engine Mode is set to CLOUD_ONLY: \(provider.displayName) API key is missing. Local fallback is strictly disallowed."
                }
                return FailingTranscriptionEngine(reason: keyMissingReason)
            }

        case .localOnly:
            if !whisperFileExists && !prefs.isWhisperModelDownloaded {
                return FailingTranscriptionEngine(
                    reason: "Engine Mode is set to LOCAL_ONLY: On-device WhisperKit model weights not installed. Cloud fallback is strictly disallowed."
                )
            } else {
                return WhisperKitEngine(modelFolderUrl: customWhisperUrl, onTokensUsed: onLocalWhisperTelemetry)
            }

        case .autoHybrid:
            let isOnline = reachabilityMonitor.isReachable
            if isOnline, let cloudEngine = makeCloudTranscriptionEngine() {
                return cloudEngine
            } else {
                if !whisperFileExists && !prefs.isWhisperModelDownloaded {
                    return FailingTranscriptionEngine(
                        reason: "Whisper transcription unavailable: \(provider.displayName) requires an API key (or active internet) and on-device WhisperKit weights are not installed."
                    )
                } else {
                    return WhisperKitEngine(modelFolderUrl: customWhisperUrl, onTokensUsed: onLocalWhisperTelemetry)
                }
            }
        }
    }

    // MARK: - Resolve Summarization Engine

    public func resolveSummarizationEngine(prefs: AppPreferences) -> any SummarizationEngine {
        let provider = prefs.selectedProvider

        let onCloudTokensUsed: GroqSummarizerEngine.TokenTelemetryCallback = { [weak self] promptTokens, completionTokens, latencyMs, providerName in
            self?.recordTokenUsage(
                promptTokens: promptTokens,
                completionTokens: completionTokens,
                latencyMs: latencyMs,
                provider: providerName,
                model: providerName,
                engineType: "CLOUD"
            )
        }

        let onLocalTokensUsed: LocalSummarizerEngine.LocalSummarizerTelemetry = { [weak self] promptTokens, completionTokens, latencyMs, providerName in
            self?.recordTokenUsage(
                promptTokens: promptTokens,
                completionTokens: completionTokens,
                latencyMs: latencyMs,
                provider: providerName,
                model: "Local LLM",
                engineType: "LOCAL"
            )
        }

        let customLlmUrl = prefs.llmModelPath.flatMap { URL(fileURLWithPath: $0) }
        let llmFileExists: Bool
        if let targetUrl = customLlmUrl {
            llmFileExists = FileManager.default.fileExists(atPath: targetUrl.path)
        } else {
            let defaultModelUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent("LocalLLM/gemma-2-2b-it.bin")
            llmFileExists = defaultModelUrl.map { FileManager.default.fileExists(atPath: $0.path) } ?? false
        }

        func makeCloudSummarizerEngine() -> (any SummarizationEngine)? {
            switch provider {
            case .groq:
                let key = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return GroqSummarizerEngine(apiKey: key, onTokensUsed: onCloudTokensUsed)

            case .openAI:
                let key = prefs.openAIApiKey.isEmpty ? KeychainVault.shared.getOpenAIApiKey() : prefs.openAIApiKey
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return OpenAISummarizerEngine(apiKey: key, candidateModels: ["gpt-4o-mini", "gpt-4o"], providerDisplayName: "OpenAI", onTokensUsed: onCloudTokensUsed)

            case .claude:
                let key = prefs.claudeApiKey.isEmpty ? KeychainVault.shared.getClaudeApiKey() : prefs.claudeApiKey
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return AnthropicClaudeSummarizerEngine(apiKey: key, candidateModels: ["claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022"], onTokensUsed: onCloudTokensUsed)

            case .custom:
                let key = prefs.customApiKey.isEmpty ? KeychainVault.shared.getCustomApiKey() : prefs.customApiKey
                let model = prefs.customSummarizerModel.trimmingCharacters(in: .whitespacesAndNewlines)
                guard !key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                return OpenAISummarizerEngine(
                    apiKey: key,
                    baseUrl: prefs.customBaseUrl,
                    candidateModels: [model.isEmpty ? "deepseek/deepseek-chat" : model],
                    providerDisplayName: "Custom (\(model.isEmpty ? "Default" : model))",
                    onTokensUsed: onCloudTokensUsed
                )
            }
        }

        switch prefs.engineMode {
        case .cloudOnly:
            if let cloudEngine = makeCloudSummarizerEngine() {
                return cloudEngine
            } else {
                return FailingSummarizerEngine(
                    reason: "Engine Mode is set to CLOUD_ONLY: \(provider.displayName) API key is missing. Local fallback is strictly disallowed."
                )
            }

        case .localOnly:
            if !llmFileExists && !prefs.isLlmModelDownloaded {
                return FailingSummarizerEngine(
                    reason: "Engine Mode is set to LOCAL_ONLY: On-device LLM model weights not installed. Cloud fallback is strictly disallowed."
                )
            } else {
                return LocalSummarizerEngine(modelFileUrl: customLlmUrl, onTokensUsed: onLocalTokensUsed)
            }

        case .autoHybrid:
            let isOnline = reachabilityMonitor.isReachable
            if isOnline, let cloudEngine = makeCloudSummarizerEngine() {
                return cloudEngine
            } else {
                if !llmFileExists && !prefs.isLlmModelDownloaded {
                    return FailingSummarizerEngine(
                        reason: "Summarization unavailable: \(provider.displayName) API key missing (or offline) and on-device LLM weights are not installed."
                    )
                } else {
                    return LocalSummarizerEngine(modelFileUrl: customLlmUrl, onTokensUsed: onLocalTokensUsed)
                }
            }
        }
    }
}
