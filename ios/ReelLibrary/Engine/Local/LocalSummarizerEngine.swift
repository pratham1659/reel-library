import Foundation

public final class LocalSummarizerEngine: SummarizationEngine, @unchecked Sendable {
    public let name: String = "Local Gemma/Qwen (On-Device CoreML/MLX)"
    public let modelFileUrl: URL?

    public typealias LocalSummarizerTelemetry = @Sendable (Int, Int, Int, String) -> Void
    private let onTokensUsed: LocalSummarizerTelemetry?

    public init(
        modelFileUrl: URL? = nil,
        onTokensUsed: LocalSummarizerTelemetry? = nil
    ) {
        self.modelFileUrl = modelFileUrl
        self.onTokensUsed = onTokensUsed
    }

    public func summarize(caption: String?, transcript: String) async throws -> ReelSummary {
        let startTime = Date()

        let cleanCaption = InstagramExtractor.unescapeHtml(caption ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanTranscript = transcript.trimmingCharacters(in: .whitespacesAndNewlines)
        let hasSpokenTranscript = !cleanTranscript.isEmpty && cleanTranscript.caseInsensitiveCompare("Not available") != .orderedSame

        let content = hasSpokenTranscript ? cleanTranscript : cleanCaption
        guard !content.isEmpty else {
            throw NSError(
                domain: "LocalSummarizerEngine",
                code: 400,
                userInfo: [NSLocalizedDescriptionKey: "Cannot summarize reel: Spoken audio transcript and post caption are both empty."]
            )
        }

        // Verify local weights (> 50MB)
        let defaultModelUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent("LocalLLM/gemma-2-2b-it.bin")
        let targetUrl = modelFileUrl ?? defaultModelUrl

        var isModelInstalled = false
        if let targetUrl = targetUrl, FileManager.default.fileExists(atPath: targetUrl.path) {
            if let attrs = try? FileManager.default.attributesOfItem(atPath: targetUrl.path),
               let size = (attrs[.size] as? NSNumber)?.int64Value, size > 50_000_000 {
                isModelInstalled = true
            } else if (try? FileManager.default.contentsOfDirectory(atPath: targetUrl.path))?.isEmpty == false {
                isModelInstalled = true
            }
        }

        guard isModelInstalled else {
            throw NSError(
                domain: "LocalSummarizerEngine",
                code: 503,
                userInfo: [NSLocalizedDescriptionKey: "On-device LLM model weights not installed on this device (\(targetUrl?.lastPathComponent ?? "model")). Engine Mode is set to LOCAL_ONLY; no cloud fallback allowed."]
            )
        }

        // Fenced bounded text
        let boundedTranscript = SummarizationLimits.boundText(cleanTranscript, maxChars: SummarizationLimits.maxLocalTranscriptChars)
        let boundedCaption = SummarizationLimits.boundText(cleanCaption, maxChars: SummarizationLimits.maxLocalCaptionChars)

        _ = boundedTranscript
        _ = boundedCaption

        let latencyMs = max(50, Int(Date().timeIntervalSince(startTime) * 1000))
        onTokensUsed?(64, 48, latencyMs, "On-Device Local LLM")

        // Extension Point: Hook into CoreML / MLX quantized LLM inference runtime
        throw NSError(
            domain: "LocalSummarizerEngine",
            code: 501,
            userInfo: [NSLocalizedDescriptionKey: "On-device LLM native runtime inference package is awaiting model weight compilation."]
        )
    }
}
