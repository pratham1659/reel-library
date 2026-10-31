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
        let apiKey = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey

        let onWhisperTelemetry: GroqWhisperEngine.AudioTelemetryCallback = { [weak self] audioSeconds, wordsCount, latencyMs in
            self?.recordTokenUsage(
                promptTokens: Int(audioSeconds),
                completionTokens: wordsCount,
                latencyMs: latencyMs,
                provider: "Groq LPU ~ Whisper",
                model: "whisper-large-v3-turbo",
                engineType: "CLOUD"
            )
        }

        let onLocalWhisperTelemetry: WhisperKitEngine.LocalWhisperTelemetry = { [weak self] prompt, comp, latencyMs, provider in
            self?.recordTokenUsage(
                promptTokens: prompt,
                completionTokens: comp,
                latencyMs: latencyMs,
                provider: provider,
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

        switch prefs.engineMode {
        case .cloudOnly:
            if !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                return GroqWhisperEngine(apiKey: apiKey, onAudioTranscribed: onWhisperTelemetry)
            } else {
                return FailingTranscriptionEngine(
                    reason: "Engine Mode is set to CLOUD_ONLY: Groq API key is missing. Local fallback is strictly disallowed."
                )
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
            if isOnline && !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                return GroqWhisperEngine(apiKey: apiKey, onAudioTranscribed: onWhisperTelemetry)
            } else {
                if !whisperFileExists && !prefs.isWhisperModelDownloaded {
                    return FailingTranscriptionEngine(
                        reason: "Whisper transcription unavailable: Cloud requires a Groq API key (or active internet) and on-device WhisperKit weights are not installed."
                    )
                } else {
                    return WhisperKitEngine(modelFolderUrl: customWhisperUrl, onTokensUsed: onLocalWhisperTelemetry)
                }
            }
        }
    }

    // MARK: - Resolve Summarization Engine

    public func resolveSummarizationEngine(prefs: AppPreferences) -> any SummarizationEngine {
        let apiKey = prefs.groqApiKey.isEmpty ? KeychainVault.shared.getGroqApiKey() : prefs.groqApiKey

        let onCloudTokensUsed: GroqSummarizerEngine.TokenTelemetryCallback = { [weak self] promptTokens, completionTokens, latencyMs, provider in
            self?.recordTokenUsage(
                promptTokens: promptTokens,
                completionTokens: completionTokens,
                latencyMs: latencyMs,
                provider: provider,
                model: provider,
                engineType: "CLOUD"
            )
        }

        let onLocalTokensUsed: LocalSummarizerEngine.LocalSummarizerTelemetry = { [weak self] promptTokens, completionTokens, latencyMs, provider in
            self?.recordTokenUsage(
                promptTokens: promptTokens,
                completionTokens: completionTokens,
                latencyMs: latencyMs,
                provider: provider,
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

        switch prefs.engineMode {
        case .cloudOnly:
            if !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                return GroqSummarizerEngine(apiKey: apiKey, onTokensUsed: onCloudTokensUsed)
            } else {
                return FailingSummarizerEngine(
                    reason: "Engine Mode is set to CLOUD_ONLY: Groq API key is missing. Local fallback is strictly disallowed."
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
            if isOnline && !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                return GroqSummarizerEngine(apiKey: apiKey, onTokensUsed: onCloudTokensUsed)
            } else {
                if !llmFileExists && !prefs.isLlmModelDownloaded {
                    return FailingSummarizerEngine(
                        reason: "Summarization unavailable: Groq API key missing (or offline) and on-device LLM weights are not installed."
                    )
                } else {
                    return LocalSummarizerEngine(modelFileUrl: customLlmUrl, onTokensUsed: onLocalTokensUsed)
                }
            }
        }
    }
}
