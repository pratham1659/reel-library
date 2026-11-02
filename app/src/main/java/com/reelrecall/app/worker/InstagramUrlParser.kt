package com.reelrecall.app.worker

object InstagramUrlParser {

    private val REEL_REGEX = """(?:https?://)?(?:www\.)?instagram\.com/(?:reel|p|share/reel)/([A-Za-z0-9_-]+)""".toRegex(RegexOption.IGNORE_CASE)
    private val HTTP_URL_REGEX = """https?://[^\s<>"]+""".toRegex(RegexOption.IGNORE_CASE)
    private val DOMAIN_URL_REGEX = """(?:https?://)?(?:www\.)?([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(?:/[^\s<>"]*)?""".toRegex(RegexOption.IGNORE_CASE)

    data class ParsedReel(
        val canonicalUrl: String,
        val shortcode: String
    )

    fun isInstagramUrl(url: String): Boolean {
        return REEL_REGEX.containsMatchIn(url)
    }

    fun extractAnyUrl(text: String): String? {
        val trimmed = text.trim()
        val httpMatch = HTTP_URL_REGEX.find(trimmed)?.value
        if (httpMatch != null) return httpMatch

        val domainMatch = DOMAIN_URL_REGEX.find(trimmed)?.value
        if (domainMatch != null) {
            return if (domainMatch.startsWith("http://", ignoreCase = true) || domainMatch.startsWith("https://", ignoreCase = true)) {
                domainMatch
            } else {
                "https://$domainMatch"
            }
        }
        return null
    }

    fun isUnsupportedUrl(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        if (parse(trimmed) != null) return false
        return extractAnyUrl(trimmed) != null
    }

    /**
     * Extracts canonical URL and shortcode from shared text.
     * Strips all tracking query parameters (?igsh=..., &utm_source=...).
     */
    fun parse(rawText: String): ParsedReel? {
        val match = REEL_REGEX.find(rawText) ?: return null
        val shortcode = match.groupValues[1]
        return ParsedReel(
            canonicalUrl = "https://www.instagram.com/reel/$shortcode/",
            shortcode = shortcode
        )
    }
}
