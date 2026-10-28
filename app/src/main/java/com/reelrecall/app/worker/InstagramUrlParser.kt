package com.reelrecall.app.worker

object InstagramUrlParser {

    private val REEL_REGEX = """https?://(?:www\.)?instagram\.com/(?:reel|p)/([A-Za-z0-9_-]+)""".toRegex(RegexOption.IGNORE_CASE)

    data class ParsedReel(
        val canonicalUrl: String,
        val shortcode: String
    )

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
