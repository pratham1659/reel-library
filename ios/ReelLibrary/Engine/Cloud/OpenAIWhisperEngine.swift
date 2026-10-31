//
//  OpenAIWhisperEngine.swift
//  ReelLibrary
//

import Foundation
import AVFoundation

public final class OpenAIWhisperEngine: TranscriptionEngine, @unchecked Sendable {
    public static let maxAudioBytes: Int64 = 25 * 1024 * 1024 // 25 MB standard OpenAI limit

    public let name: String
    private let apiKey: String
    private let baseUrl: String
    private let model: String
    private let urlSession: URLSession

    public typealias AudioTelemetryCallback = @Sendable (Double, Int, Int) -> Void
    private let onAudioTranscribed: AudioTelemetryCallback?

    public init(
        apiKey: String,
        baseUrl: String = "https://api.openai.com/v1",
        model: String = "whisper-1",
        providerDisplayName: String = "OpenAI Whisper",
        urlSession: URLSession = .shared,
        onAudioTranscribed: AudioTelemetryCallback? = nil
    ) {
        self.apiKey = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        self.baseUrl = baseUrl.trimmingCharacters(in: .whitespacesAndNewlines).trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.model = model.isEmpty ? "whisper-1" : model
        self.name = providerDisplayName
        self.urlSession = urlSession
        self.onAudioTranscribed = onAudioTranscribed
    }

    public func transcribe(audioFile: URL) async throws -> String {
        let startTime = Date()

        guard !apiKey.isEmpty else {
            throw NSError(
                domain: "OpenAIWhisperEngine",
                code: 401,
                userInfo: [NSLocalizedDescriptionKey: "\(name) API key is not configured in Settings."]
            )
        }

        let fileManager = FileManager.default
        guard fileManager.fileExists(atPath: audioFile.path) else {
            throw NSError(
                domain: "OpenAIWhisperEngine",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "No valid audio stream available for transcription at \(audioFile.path)"]
            )
        }

        let attributes = try fileManager.attributesOfItem(atPath: audioFile.path)
        let fileSize = (attributes[.size] as? NSNumber)?.int64Value ?? 0

        guard fileSize > 0 else {
            throw NSError(
                domain: "OpenAIWhisperEngine",
                code: 400,
                userInfo: [NSLocalizedDescriptionKey: "Audio file is empty."]
            )
        }

        // Hard audio size guard: 25 MB limit
        if fileSize > Self.maxAudioBytes {
            let sizeInMB = Double(fileSize) / (1024.0 * 1024.0)
            throw NSError(
                domain: "OpenAIWhisperEngine",
                code: 413,
                userInfo: [NSLocalizedDescriptionKey: String(format: "Audio file exceeds maximum upload limit (25 MB). Size: %.1f MB", sizeInMB)]
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

        let cleanBase = baseUrl.isEmpty ? "https://api.openai.com/v1" : baseUrl
        let endpointStr = cleanBase.hasSuffix("/audio/transcriptions") ? cleanBase : "\(cleanBase)/audio/transcriptions"

        guard let endpoint = URL(string: endpointStr) else {
            throw NSError(
                domain: "OpenAIWhisperEngine",
                code: 400,
                userInfo: [NSLocalizedDescriptionKey: "Invalid API endpoint: \(endpointStr)"]
            )
        }

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
        body.appendMultipartForm(boundary: boundary, name: "model", value: model)
        body.appendMultipartForm(boundary: boundary, name: "response_format", value: "json")
        body.appendMultipartForm(boundary: boundary, name: "prompt", value: whisperPrompt)
        body.appendMultipartForm(boundary: boundary, name: "file", filename: filename, mimeType: mimeType, data: audioData)
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
                domain: "OpenAIWhisperEngine",
                code: httpResponse.statusCode,
                userInfo: [NSLocalizedDescriptionKey: "\(name) failed (\(httpResponse.statusCode)): \(errorBody)"]
            )
        }

        struct WhisperResponse: Codable {
            let text: String
        }

        let parsed = try JSONDecoder().decode(WhisperResponse.self, from: data)
        let finalizedTranscript = parsed.text.trimmingCharacters(in: .whitespacesAndNewlines)

        let wordsCount = finalizedTranscript
            .split { $0.isWhitespace || $0.isNewline }
            .filter { !$0.isEmpty }
            .count

        onAudioTranscribed?(audioDurationSeconds, wordsCount, latencyMs)

        return finalizedTranscript
    }
}

private extension Data {
    mutating func appendMultipartForm(boundary: String, name: String, value: String) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n".data(using: .utf8)!)
        append("\(value)\r\n".data(using: .utf8)!)
    }

    mutating func appendMultipartForm(boundary: String, name: String, filename: String, mimeType: String, data: Data) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        append(data)
        append("\r\n".data(using: .utf8)!)
    }
}
