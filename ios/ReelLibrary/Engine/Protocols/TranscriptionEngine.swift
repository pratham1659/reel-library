import Foundation

public protocol TranscriptionEngine: Sendable {
    var name: String { get }
    func transcribe(audioFile: URL) async throws -> String
}

public struct FailingTranscriptionEngine: TranscriptionEngine, Sendable {
    public let name: String = "Transcription Engine Error"
    public let reason: String

    public init(reason: String) {
        self.reason = reason
    }

    public func transcribe(audioFile: URL) async throws -> String {
        throw NSError(
            domain: "TranscriptionEngine",
            code: -1,
            userInfo: [NSLocalizedDescriptionKey: reason]
        )
    }
}
