import Foundation

public final class WhisperKitEngine: TranscriptionEngine, @unchecked Sendable {
    public let name: String = "Local WhisperKit (On-Device CoreML)"
    public let modelFolderUrl: URL?

    public typealias LocalWhisperTelemetry = @Sendable (Int, Int, Int, String) -> Void
    private let onTokensUsed: LocalWhisperTelemetry?

    public init(
        modelFolderUrl: URL? = nil,
        onTokensUsed: LocalWhisperTelemetry? = nil
    ) {
        self.modelFolderUrl = modelFolderUrl
        self.onTokensUsed = onTokensUsed
    }

    public func transcribe(audioFile: URL) async throws -> String {
        let startTime = Date()

        // Verify audio file exists and is readable
        guard FileManager.default.fileExists(atPath: audioFile.path) else {
            throw NSError(
                domain: "WhisperKitEngine",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "Audio file not found at \(audioFile.path)"]
            )
        }

        // Verify local model weights exist (> 10MB)
        let resolvedModelUrl = modelFolderUrl ?? FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent("WhisperKit/whisper-base")

        var isModelInstalled = false
        if let targetUrl = resolvedModelUrl {
            if FileManager.default.fileExists(atPath: targetUrl.path) {
                if let attrs = try? FileManager.default.attributesOfItem(atPath: targetUrl.path),
                   let size = (attrs[.size] as? NSNumber)?.int64Value, size > 10_000_000 {
                    isModelInstalled = true
                } else if (try? FileManager.default.contentsOfDirectory(atPath: targetUrl.path))?.isEmpty == false {
                    // Bundle/directory containing CoreML model assets
                    isModelInstalled = true
                }
            }
        }

        guard isModelInstalled else {
            throw NSError(
                domain: "WhisperKitEngine",
                code: 503,
                userInfo: [NSLocalizedDescriptionKey: TranscriptOutcome.localUnimplemented]
            )
        }

        let latencyMs = max(32, Int(Date().timeIntervalSince(startTime) * 1000))
        onTokensUsed?(0, 0, latencyMs, "On-Device WhisperKit")

        // Extension Point: Hook into CoreML WhisperKit inference pipeline when integrated
        throw NSError(
            domain: "WhisperKitEngine",
            code: 501,
            userInfo: [NSLocalizedDescriptionKey: TranscriptOutcome.localUnimplemented]
        )
    }
}
