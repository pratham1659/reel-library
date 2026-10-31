import Foundation
import AVFoundation

public final class GroqWhisperEngine: TranscriptionEngine, @unchecked Sendable {
    public static let maxGroqAudioBytes: Int64 = 25 * 1024 * 1024 // 25 MB limit

    public let name: String = "Groq LPU ~ Whisper"
    private let apiKey: String
    private let urlSession: URLSession
    public typealias AudioTelemetryCallback = @Sendable (Double, Int, Int) -> Void
    private let onAudioTranscribed: AudioTelemetryCallback?

    public init(
        apiKey: String,
        urlSession: URLSession = .shared,
        onAudioTranscribed: AudioTelemetryCallback? = nil
    ) {
        self.apiKey = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        self.urlSession = urlSession
        self.onAudioTranscribed = onAudioTranscribed
    }

    public func transcribe(audioFile: URL) async throws -> String {
        let startTime = Date()

        guard !apiKey.isEmpty else {
            throw NSError(
                domain: "GroqWhisperEngine",
                code: 401,
                userInfo: [NSLocalizedDescriptionKey: "Groq API key is not configured in Settings."]
            )
        }

        let fileManager = FileManager.default
        guard fileManager.fileExists(atPath: audioFile.path) else {
            throw NSError(
                domain: "GroqWhisperEngine",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "No valid audio stream available for transcription at \(audioFile.path)"]
            )
        }

        let attributes = try fileManager.attributesOfItem(atPath: audioFile.path)
        let fileSize = (attributes[.size] as? NSNumber)?.int64Value ?? 0

        guard fileSize > 0 else {
            throw NSError(
                domain: "GroqWhisperEngine",
                code: 400,
                userInfo: [NSLocalizedDescriptionKey: "Audio file is empty."]
            )
        }

        // Hard audio size guard: 25 MB limit
        if fileSize > Self.maxGroqAudioBytes {
            let sizeInMB = Double(fileSize) / (1024.0 * 1024.0)
            throw NSError(
                domain: "GroqWhisperEngine",
                code: 413,
                userInfo: [NSLocalizedDescriptionKey: String(format: "Audio file exceeds Groq maximum upload limit (25 MB). Size: %.1f MB", sizeInMB)]
            )
        }

        let audioData = try Data(contentsOf: audioFile)

        // Read audio duration
        var audioDurationSeconds: Double = 1.0
        let asset = AVURLAsset(url: audioFile)
        if let cmDuration = try? await asset.load(.duration) {
            let seconds = CMTimeGetSeconds(cmDuration)
            if seconds.isFinite && seconds > 0 {
                audioDurationSeconds = seconds
            }
        }

        let endpoint = URL(string: "https://api.groq.com/openai/v1/audio/transcriptions")!
        var request = URLRequest(url: endpoint)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 60.0

        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        let whisperPrompt = "Transcribe spoken Hindi in Hinglish using Roman Latin script. Transcribe English in English. For any other foreign language, translate and transcribe in English."

        let mimeType = audioFile.pathExtension.lowercased() == "mp4" ? "video/mp4" : "audio/m4a"
        let filename = audioFile.lastPathComponent.isEmpty ? "audio.m4a" : audioFile.lastPathComponent

        var body = Data()
        // Model field
        body.appendMultipart(boundary: boundary, name: "model", value: "whisper-large-v3-turbo")
        // Response format field
        body.appendMultipart(boundary: boundary, name: "response_format", value: "json")
        // Prompt field
        body.appendMultipart(boundary: boundary, name: "prompt", value: whisperPrompt)
        // File field
        body.appendMultipart(boundary: boundary, name: "file", filename: filename, mimeType: mimeType, data: audioData)
        // Close boundary
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)

        request.httpBody = body

        let (data, response) = try await urlSession.data(for: request)
        let latencyMs = Int(Date().timeIntervalSince(startTime) * 1000)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            let errorBody = String(data: data, encoding: .utf8) ?? "Unknown error"
            throw NSError(
                domain: "GroqWhisperEngine",
                code: httpResponse.statusCode,
                userInfo: [NSLocalizedDescriptionKey: "Groq Transcription failed (\(httpResponse.statusCode)): \(errorBody)"]
            )
        }

        struct WhisperResponse: Codable {
            let text: String
        }

        let parsed = try JSONDecoder().decode(WhisperResponse.self, from: data)
        let rawTranscript = parsed.text.trimmingCharacters(in: .whitespacesAndNewlines)

        // Devanagari transliteration check:
        // If transcript contains Devanagari Unicode \u0900..\u097F, invoke llama-3.1-8b-instant to transliterate to Roman Latin Hinglish
        let finalizedTranscript = await formatTranscriptHinglishOrEnglish(rawTranscript)

        let wordsCount = finalizedTranscript
            .split { $0.isWhitespace || $0.isNewline }
            .filter { !$0.isEmpty }
            .count

        onAudioTranscribed?(audioDurationSeconds, wordsCount, latencyMs)

        return finalizedTranscript
    }

    // MARK: - Devanagari Transliteration Check

    private func formatTranscriptHinglishOrEnglish(_ rawText: String) async -> String {
        guard !rawText.isEmpty, !apiKey.isEmpty else { return rawText }

        // Check for Devanagari range \u{0900}...\u{097F}
        let hasDevanagari = rawText.unicodeScalars.contains { scalar in
            scalar.value >= 0x0900 && scalar.value <= 0x097F
        }

        guard hasDevanagari else {
            return rawText
        }

        let systemPrompt = """
        You are an expert audio transcription formatter. Follow these strict rules:
        1. If the content is in Hindi (Devanagari script), transliterate/convert it into natural, fluent HINGLISH using Roman/Latin alphabet (e.g., 'Haan dosto aaj hum baat karenge...'). Keep technical English terms in English.
        2. If the content is in English, output it in English.
        3. If the content is in another non-English foreign language, translate it into English.
        4. Output ONLY the finalized text. Do not add quotes, introductory remarks, or explanations.
        """

        let payload: [String: Any] = [
            "model": "llama-3.1-8b-instant",
            "messages": [
                ["role": "system", "content": systemPrompt],
                ["role": "user", "content": rawText]
            ],
            "temperature": 0.1,
            "max_tokens": 1024
        ]

        guard let requestBody = try? JSONSerialization.data(withJSONObject: payload),
              let url = URL(string: "https://api.groq.com/openai/v1/chat/completions") else {
            return rawText
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = requestBody
        request.timeoutInterval = 20.0

        do {
            let (data, response) = try await urlSession.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
                return rawText
            }

            if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let choices = json["choices"] as? [[String: Any]],
               let message = choices.first?["message"] as? [String: Any],
               let content = message["content"] as? String {
                let trimmed = content.trimmingCharacters(in: .whitespacesAndNewlines)
                return trimmed.isEmpty ? rawText : trimmed
            }
        } catch {
            // Fallback to raw text if transliteration fails
        }

        return rawText
    }
}

private extension Data {
    mutating func appendMultipart(boundary: String, name: String, value: String) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n".data(using: .utf8)!)
        append("\(value)\r\n".data(using: .utf8)!)
    }

    mutating func appendMultipart(boundary: String, name: String, filename: String, mimeType: String, data: Data) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        append(data)
        append("\r\n".data(using: .utf8)!)
    }
}
