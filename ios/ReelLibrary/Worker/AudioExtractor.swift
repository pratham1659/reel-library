import Foundation
@preconcurrency import AVFoundation

public enum AudioExtractionError: LocalizedError, Sendable {
    case assetLoadingFailed(String)
    case noAudioTrack
    case exportSessionCreationFailed
    case exportFailed(String)
    case cancelled

    public var errorDescription: String? {
        switch self {
        case .assetLoadingFailed(let msg):
            return "Asset loading failed: \(msg)"
        case .noAudioTrack:
            return "The media asset contains no extractable audio tracks."
        case .exportSessionCreationFailed:
            return "Could not initialize AVAssetExportSession with M4A preset."
        case .exportFailed(let msg):
            return "Audio export failed: \(msg)"
        case .cancelled:
            return "Audio export was cancelled."
        }
    }
}

public struct AudioExtractionResult: Sendable {
    public let audioUrl: URL
    public let durationSeconds: Double

    public init(audioUrl: URL, durationSeconds: Double) {
        self.audioUrl = audioUrl
        self.durationSeconds = durationSeconds
    }
}

public actor AudioExtractor: Sendable {
    public init() {}

    /// Extracts audio from a local or remote video asset into a compact `.m4a` file.
    /// Uses `AVAssetExportPresetAppleM4A` for sub-100ms hardware-accelerated processing.
    /// - Parameters:
    ///   - videoUrl: Local file URL or remote video URL.
    ///   - outputDirectory: Directory where the `.m4a` file will be saved. Defaults to temp directory.
    /// - Returns: `AudioExtractionResult` containing the `.m4a` file URL and its duration in seconds.
    public func extractAudio(
        from videoUrl: URL,
        outputDirectory: URL? = nil
    ) async throws -> AudioExtractionResult {
        let asset = AVURLAsset(url: videoUrl, options: [AVURLAssetPreferPreciseDurationAndTimingKey: true])

        // Verify audio tracks exist
        let audioTracks = try await asset.loadTracks(withMediaType: .audio)
        guard !audioTracks.isEmpty else {
            throw AudioExtractionError.noAudioTrack
        }

        // Load asset duration asynchronously
        let cmDuration = try await asset.load(.duration)
        let durationSeconds = CMTimeGetSeconds(cmDuration)

        // Prepare destination file
        let directory = outputDirectory ?? FileManager.default.temporaryDirectory
        let destinationUrl = directory
            .appendingPathComponent("reel_audio_\(UUID().uuidString)")
            .appendingPathExtension("m4a")

        // Remove destination file if already present
        if FileManager.default.fileExists(atPath: destinationUrl.path) {
            try? FileManager.default.removeItem(at: destinationUrl)
        }

        guard let exportSession = AVAssetExportSession(
            asset: asset,
            presetName: AVAssetExportPresetAppleM4A
        ) else {
            throw AudioExtractionError.exportSessionCreationFailed
        }

        exportSession.outputURL = destinationUrl
        exportSession.outputFileType = .m4a
        exportSession.shouldOptimizeForNetworkUse = false

        nonisolated(unsafe) let sessionRef = exportSession
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            sessionRef.exportAsynchronously {
                switch sessionRef.status {
                case .completed:
                    continuation.resume()
                case .failed:
                    let err = sessionRef.error?.localizedDescription ?? "Unknown AVAssetExportSession error"
                    continuation.resume(throwing: AudioExtractionError.exportFailed(err))
                case .cancelled:
                    continuation.resume(throwing: AudioExtractionError.cancelled)
                default:
                    let err = sessionRef.error?.localizedDescription ?? "Unexpected status: \(sessionRef.status.rawValue)"
                    continuation.resume(throwing: AudioExtractionError.exportFailed(err))
                }
            }
        }

        return AudioExtractionResult(audioUrl: destinationUrl, durationSeconds: max(0.1, durationSeconds))
    }
}
