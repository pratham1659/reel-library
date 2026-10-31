import Foundation

public struct ParsedReel: Sendable, Equatable {
    public let canonicalUrl: String
    public let shortcode: String

    public init(canonicalUrl: String, shortcode: String) {
        self.canonicalUrl = canonicalUrl
        self.shortcode = shortcode
    }
}

public enum InstagramUrlParser: Sendable {
    private static let reelRegex: NSRegularExpression = {
        let pattern = #"https?://(?:[a-zA-Z0-9_\.]+\.)?instagram\.com/(?:reel|reels|p|share/reel)/([A-Za-z0-9_-]+)"#
        // Safe forced unwrap on constant pattern
        return try! NSRegularExpression(pattern: pattern, options: [.caseInsensitive])
    }()

    /// Extracts canonical URL (`https://www.instagram.com/reel/<shortcode>/`) and shortcode from raw text or URLs.
    /// Automatically strips all tracking query parameters (?igsh=..., &utm_source=...).
    public static func parse(_ rawText: String) -> ParsedReel? {
        let trimmed = rawText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }

        let nsRange = NSRange(trimmed.startIndex..<trimmed.endIndex, in: trimmed)
        guard let match = reelRegex.firstMatch(in: trimmed, options: [], range: nsRange),
              match.numberOfRanges > 1,
              let shortcodeRange = Range(match.range(at: 1), in: trimmed) else {
            return nil
        }

        let shortcode = String(trimmed[shortcodeRange])
        guard !shortcode.isEmpty else { return nil }

        let canonical = "https://www.instagram.com/reel/\(shortcode)/"
        return ParsedReel(canonicalUrl: canonical, shortcode: shortcode)
    }
}
