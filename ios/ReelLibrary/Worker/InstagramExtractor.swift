import Foundation

public struct ExtractedMetadata: Sendable {
    public let caption: String
    public let creatorHandle: String
    public let videoUrl: String?
    public let thumbnailUrl: String?
    public let downloadedVideoFile: URL?

    public init(
        caption: String,
        creatorHandle: String,
        videoUrl: String?,
        thumbnailUrl: String?,
        downloadedVideoFile: URL? = nil
    ) {
        self.caption = caption
        self.creatorHandle = creatorHandle
        self.videoUrl = videoUrl
        self.thumbnailUrl = thumbnailUrl
        self.downloadedVideoFile = downloadedVideoFile
    }
}

public actor InstagramExtractor: Sendable {
    private let urlSession: URLSession

    private static let facebookUserAgent = "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"
    private static let safariMobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

    public init(urlSession: URLSession = .shared) {
        self.urlSession = urlSession
    }

    /// Scrapes OpenGraph tags from the canonical Instagram reel URL, falling back to captioned embed if needed.
    /// - Parameters:
    ///   - canonicalUrl: Normalized URL in the form `https://www.instagram.com/reel/<shortcode>/`
    ///   - downloadVideo: Whether to also download the extracted video file to a temporary location for audio processing.
    public func extract(canonicalUrl: String, downloadVideo: Bool = true) async throws -> ExtractedMetadata {
        guard let url = URL(string: canonicalUrl),
              url.scheme?.lowercased() == "https",
              let host = url.host?.lowercased(),
              host == "instagram.com" || host.hasSuffix(".instagram.com") else {
            throw URLError(.badURL)
        }

        var caption: String?
        var creator: String?
        var videoUrl: String?
        var thumbnailUrl: String?

        // Step 1: Scrape OpenGraph metadata with Facebook externalhit User-Agent
        var ogRequest = URLRequest(url: url)
        ogRequest.setValue(Self.facebookUserAgent, forHTTPHeaderField: "User-Agent")
        ogRequest.setValue("en-US,en;q=0.9", forHTTPHeaderField: "Accept-Language")
        ogRequest.timeoutInterval = 15.0

        if let (data, response) = try? await urlSession.data(for: ogRequest),
           let httpResponse = response as? HTTPURLResponse,
           (200...299).contains(httpResponse.statusCode),
           let html = String(data: data, encoding: .utf8) {

            // Extract og:description (caption)
            if let rawDesc = Self.extractMetaContent(property: "og:description", from: html) {
                let unescaped = Self.unescapeHtml(rawDesc).trimmingCharacters(in: .whitespacesAndNewlines)
                caption = Self.cleanBoilerplateCaption(unescaped)
            }

            // Extract og:image or twitter:image (thumbnail)
            if let rawImage = Self.extractMetaContent(property: "og:image", from: html) ??
                Self.extractMetaContent(name: "twitter:image", from: html) {
                thumbnailUrl = rawImage.replacingOccurrences(of: "&amp;", with: "&")
            }

            // Extract creator handle from twitter:title or og:title
            if let rawTwitterTitle = Self.extractMetaContent(name: "twitter:title", from: html) {
                let unescaped = Self.unescapeHtml(rawTwitterTitle).trimmingCharacters(in: .whitespacesAndNewlines)
                creator = Self.extractCreatorHandle(from: unescaped)
            }

            if creator == nil, let rawTitle = Self.extractMetaContent(property: "og:title", from: html) {
                let unescaped = Self.unescapeHtml(rawTitle).trimmingCharacters(in: .whitespacesAndNewlines)
                creator = Self.extractCreatorHandle(from: unescaped)
            }

            // Extract keywords as fallback caption if empty
            if (caption == nil || caption?.isEmpty == true),
               let keywords = Self.extractMetaContent(name: "keywords", from: html) {
                let cleanKeywords = Self.unescapeHtml(keywords).trimmingCharacters(in: .whitespacesAndNewlines)
                if !cleanKeywords.isEmpty {
                    caption = cleanKeywords
                }
            }

            // Extract og:video or og:video:secure_url
            if let rawVideo = Self.extractMetaContent(property: "og:video", from: html) ??
                Self.extractMetaContent(property: "og:video:secure_url", from: html) {
                videoUrl = Self.cleanVideoUrl(rawVideo)
            }

            // Fallback inside HTML for JSON video_url
            if videoUrl == nil || videoUrl?.isEmpty == true {
                videoUrl = Self.extractJsonVideoUrl(from: html)
            }
        }

        // Step 2: Fallback to embed endpoint if videoUrl is still missing
        if videoUrl == nil || videoUrl?.isEmpty == true {
            let embedEndpoint = canonicalUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/embed/captioned/"
            if let embedUrl = URL(string: embedEndpoint) {
                var embedRequest = URLRequest(url: embedUrl)
                embedRequest.setValue(Self.safariMobileUserAgent, forHTTPHeaderField: "User-Agent")
                embedRequest.setValue("en-US,en;q=0.9", forHTTPHeaderField: "Accept-Language")
                embedRequest.timeoutInterval = 15.0

                if let (embedData, embedResponse) = try? await urlSession.data(for: embedRequest),
                   let httpResponse = embedResponse as? HTTPURLResponse,
                   (200...299).contains(httpResponse.statusCode),
                   let embedHtml = String(data: embedData, encoding: .utf8) {
                    videoUrl = Self.extractJsonVideoUrl(from: embedHtml)
                }
            }
        }

        // Step 3: Download video file if requested and videoUrl is available
        var downloadedFileUrl: URL?
        if downloadVideo, let validVideoUrl = videoUrl, let remoteMediaUrl = URL(string: validVideoUrl) {
            downloadedFileUrl = try? await downloadMediaFile(from: remoteMediaUrl)
        }

        let finalCaption = (caption?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false)
            ? caption!
            : "Instagram Reel shared to Reel Library."
        let finalCreator = (creator?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false)
            ? creator!
            : "@instagram_creator"

        return ExtractedMetadata(
            caption: finalCaption,
            creatorHandle: finalCreator,
            videoUrl: videoUrl,
            thumbnailUrl: thumbnailUrl,
            downloadedVideoFile: downloadedFileUrl
        )
    }

    // MARK: - Video Downloader

    private func downloadMediaFile(from url: URL) async throws -> URL {
        guard url.scheme?.lowercased() == "https",
              let host = url.host?.lowercased(),
              host.contains("instagram.com") || host.contains("cdninstagram.com") || host.contains("fbcdn.net") else {
            throw URLError(.unsupportedURL)
        }

        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30.0

        let (tempLocalUrl, response) = try await urlSession.download(for: request)
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            throw URLError(.badServerResponse)
        }

        // Maximum video download guard: 100 MB
        let maxDownloadBytes: Int64 = 100 * 1024 * 1024
        let fileManager = FileManager.default
        let attributes = try? fileManager.attributesOfItem(atPath: tempLocalUrl.path)
        let downloadedBytes = (attributes?[.size] as? NSNumber)?.int64Value ?? 0
        if downloadedBytes > maxDownloadBytes {
            try? fileManager.removeItem(at: tempLocalUrl)
            throw URLError(.dataLengthExceedsMaximum)
        }

        let destination = fileManager.temporaryDirectory
            .appendingPathComponent("reel_video_\(UUID().uuidString)")
            .appendingPathExtension("mp4")

        try fileManager.moveItem(at: tempLocalUrl, to: destination)
        return destination
    }

    // MARK: - HTML Regex Parsing & Cleansing

    public static func extractMetaContent(property: String? = nil, name: String? = nil, from html: String) -> String? {
        let pattern: String
        if let property = property {
            pattern = #"<meta\s+property="\#(property)"\s+content="([^"]*)"\s*/?>"#
        } else if let name = name {
            pattern = #"<meta\s+name="\#(name)"\s+content="([^"]*)"\s*/?>"#
        } else {
            return nil
        }

        guard let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else {
            return nil
        }

        let range = NSRange(html.startIndex..<html.endIndex, in: html)
        guard let match = regex.firstMatch(in: html, options: [], range: range),
              match.numberOfRanges > 1,
              let contentRange = Range(match.range(at: 1), in: html) else {
            return nil
        }

        return String(html[contentRange])
    }

    public static func cleanBoilerplateCaption(_ rawCaption: String) -> String {
        var cleaned = rawCaption

        // Check for pattern: '13K likes, 10K comments - username on date: "Actual Caption"'
        if cleaned.contains(": \"") {
            if let afterQuote = cleaned.components(separatedBy: ": \"").last {
                var stripped = afterQuote.trimmingCharacters(in: .whitespacesAndNewlines)
                if stripped.hasSuffix("\".") {
                    stripped.removeLast(2)
                } else if stripped.hasSuffix("\"") {
                    stripped.removeLast(1)
                }
                cleaned = stripped.trimmingCharacters(in: .whitespacesAndNewlines)
            }
        } else if cleaned.contains(" - ") && cleaned.contains(": ") {
            if let afterColon = cleaned.components(separatedBy: ": ").last {
                cleaned = afterColon.trimmingCharacters(in: .whitespacesAndNewlines)
            }
        }

        return cleaned
    }

    public static func extractCreatorHandle(from titleText: String) -> String? {
        // Look for @handle regex first
        if let atRange = titleText.range(of: #"@[a-zA-Z0-9._]+"#, options: .regularExpression) {
            return String(titleText[atRange])
        }

        // Look for "username on Instagram:"
        if titleText.contains("on Instagram:") {
            let prefix = titleText.components(separatedBy: "on Instagram:").first?.trimmingCharacters(in: .whitespacesAndNewlines)
            if let p = prefix, !p.isEmpty {
                return p.hasPrefix("@") ? p : "@\(p)"
            }
        }

        // Fallback before bullet " •"
        if titleText.contains(" •") {
            let prefix = titleText.components(separatedBy: " •").first?.trimmingCharacters(in: .whitespacesAndNewlines)
            if let p = prefix, !p.isEmpty {
                return p.hasPrefix("@") ? p : "@\(p)"
            }
        }

        return nil
    }

    public static func cleanVideoUrl(_ rawUrl: String) -> String {
        return rawUrl
            .replacingOccurrences(of: "\\/", with: "/")
            .replacingOccurrences(of: "\\u0025", with: "%")
            .replacingOccurrences(of: "\\u0026", with: "&")
            .replacingOccurrences(of: "&amp;", with: "&")
            .replacingOccurrences(of: "\\", with: "")
    }

    public static func extractJsonVideoUrl(from html: String) -> String? {
        let patterns = [
            #"["']video_url["']\s*:\s*["'](https:[^"'\s\\]+[\s\S]*?)["']"#,
            #"(https:[\\/]+[a-zA-Z0-9.\-_\\/]+?\.mp4[a-zA-Z0-9.\-_?&=%\\/]*)"#
        ]

        for pattern in patterns {
            if let regex = try? NSRegularExpression(pattern: pattern, options: []) {
                let range = NSRange(html.startIndex..<html.endIndex, in: html)
                if let match = regex.firstMatch(in: html, options: [], range: range),
                   match.numberOfRanges > 1,
                   let matchedRange = Range(match.range(at: 1), in: html) {
                    let matched = String(html[matchedRange])
                    let cleaned = cleanVideoUrl(matched)
                    if cleaned.contains(".mp4") {
                        return cleaned
                    }
                }
            }
        }
        return nil
    }

    public static func unescapeHtml(_ string: String) -> String {
        var result = string
        let entities = [
            ("&amp;", "&"),
            ("&quot;", "\""),
            ("&apos;", "'"),
            ("&#39;", "'"),
            ("&lt;", "<"),
            ("&gt;", ">"),
            ("&nbsp;", " ")
        ]
        for (entity, replacement) in entities {
            result = result.replacingOccurrences(of: entity, with: replacement)
        }

        // Numeric entity decoding: &#123; or &#x1F600;
        let regexPattern = #"&#(x?[0-9a-fA-F]+);"#
        if let regex = try? NSRegularExpression(pattern: regexPattern, options: []) {
            let nsRange = NSRange(result.startIndex..<result.endIndex, in: result)
            let matches = regex.matches(in: result, options: [], range: nsRange)
            for match in matches.reversed() {
                guard let fullRange = Range(match.range(at: 0), in: result),
                      let valueRange = Range(match.range(at: 1), in: result) else { continue }
                let valueStr = String(result[valueRange])
                let charCode: UInt32?
                if valueStr.lowercased().hasPrefix("x") {
                    let hex = String(valueStr.dropFirst())
                    charCode = UInt32(hex, radix: 16)
                } else {
                    charCode = UInt32(valueStr, radix: 10)
                }
                if let code = charCode, let scalar = UnicodeScalar(code) {
                    result.replaceSubrange(fullRange, with: String(Character(scalar)))
                }
            }
        }

        return result
    }
}
