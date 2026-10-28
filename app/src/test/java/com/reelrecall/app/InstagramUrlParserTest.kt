package com.reelrecall.app

import com.reelrecall.app.worker.InstagramUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class InstagramUrlParserTest {

    @Test
    fun parse_cleanReelUrl_extractsSuccessfully() {
        val input = "https://www.instagram.com/reel/C8XYZ123abc/"
        val parsed = InstagramUrlParser.parse(input)
        assertNotNull(parsed)
        assertEquals("https://www.instagram.com/reel/C8XYZ123abc/", parsed?.canonicalUrl)
        assertEquals("C8XYZ123abc", parsed?.shortcode)
    }

    @Test
    fun parse_dirtyReelUrlWithTrackingAndText_extractsCanonicalUrl() {
        val input = "Look at this amazing reel: https://www.instagram.com/reel/DF99kLk1/?igsh=MWQ1ZGRldjFh&utm_source=ig_web_copy_link Shared via IG"
        val parsed = InstagramUrlParser.parse(input)
        assertNotNull(parsed)
        assertEquals("https://www.instagram.com/reel/DF99kLk1/", parsed?.canonicalUrl)
        assertEquals("DF99kLk1", parsed?.shortcode)
    }

    @Test
    fun parse_postFormatUrl_extractsSuccessfully() {
        val input = "https://instagram.com/p/C99ABC456/?utm_medium=share_sheet"
        val parsed = InstagramUrlParser.parse(input)
        assertNotNull(parsed)
        assertEquals("https://www.instagram.com/reel/C99ABC456/", parsed?.canonicalUrl)
        assertEquals("C99ABC456", parsed?.shortcode)
    }

    @Test
    fun parse_invalidUrl_returnsNull() {
        val input = "https://www.youtube.com/watch?v=12345"
        val parsed = InstagramUrlParser.parse(input)
        assertNull(parsed)
    }
}
