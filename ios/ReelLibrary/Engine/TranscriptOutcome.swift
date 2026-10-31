import Foundation

/// What ingest stores for one reel's spoken audio.
/// `text` is nil unless speech was actually returned. `note` is the user-facing reason.
/// The sentinel "Not available" is never stored.
public struct TranscriptOutcome: Equatable, Sendable {
    public let text: String?
    public let note: String?

    public static let noMedia = "Instagram did not return a downloadable video, so there is no audio to transcribe."
    public static let noSpeech = "The audio had no recognizable speech."
    public static let localUnimplemented = "On-device Whisper is not implemented in this build. Switch to Cloud and add a Groq or OpenAI key."
    public static let migrated = "Spoken audio was not transcribed on a previous import. Retry transcription."
    public static let captionOnly = "Spoken audio transcript is not available for this reel. AI Summary was generated directly from the post caption."
    public static let maxUploadBytes: Int64 = 25 * 1024 * 1024

    public var summaryInput: String {
        text ?? ""
    }

    public init(text: String?, note: String?) {
        self.text = text
        self.note = note
    }

    public static func ready(_ text: String) -> TranscriptOutcome {
        TranscriptOutcome(text: text, note: nil)
    }

    public static func unavailable(_ note: String) -> TranscriptOutcome {
        TranscriptOutcome(text: nil, note: note)
    }

    /// - Parameter audioBytes: nil when no audio file exists.
    /// - Parameter transcriptionText: set only when the engine returned text. Pass nil with `transcriptionError` on failure.
    public static func resolve(
        mediaNote: String?,
        audioBytes: Int64?,
        transcriptionText: String?,
        transcriptionError: String?,
        cloudUpload: Bool
    ) -> TranscriptOutcome {
        guard let audioBytes, audioBytes > 0 else {
            let note = mediaNote?.trimmingCharacters(in: .whitespacesAndNewlines)
            return unavailable((note?.isEmpty == false) ? note! : noMedia)
        }
        if cloudUpload && audioBytes > maxUploadBytes {
            let megabytes = audioBytes / (1024 * 1024)
            return unavailable("Audio file exceeds the 25 MB upload limit. Size: \(megabytes) MB")
        }
        if let transcriptionError {
            let note = transcriptionError.trimmingCharacters(in: .whitespacesAndNewlines)
            return unavailable(note.isEmpty ? "Transcription failed." : note)
        }
        let trimmed = transcriptionText?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if trimmed.isEmpty || trimmed.caseInsensitiveCompare("Not available") == .orderedSame {
            return unavailable(noSpeech)
        }
        return ready(trimmed)
    }

    public static func hasSpokenTranscript(_ transcript: String?) -> Bool {
        let spoken = transcript?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return !spoken.isEmpty && spoken.caseInsensitiveCompare("Not available") != .orderedSame
    }
}
