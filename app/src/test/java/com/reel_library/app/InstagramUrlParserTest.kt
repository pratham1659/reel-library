package com.reel_library.app

import com.reel_library.app.worker.InstagramUrlParser
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
    fun parse_targetUserReelUrl_extractsSuccessfully() {
        val input = "https://www.instagram.com/reel/DfFihWAsB_M/?rpxt=Nzdqem5wOG9tdXVl"
        val parsed = InstagramUrlParser.parse(input)
        assertNotNull(parsed)
        assertEquals("https://www.instagram.com/reel/DfFihWAsB_M/", parsed?.canonicalUrl)
        assertEquals("DfFihWAsB_M", parsed?.shortcode)
    }

    @Test
    fun parse_invalidUrl_returnsNull() {
        val input = "https://www.youtube.com/watch?v=12345"
        val parsed = InstagramUrlParser.parse(input)
        assertNull(parsed)
    }

    @Test
    fun parse_reelWithoutHttpProtocol_extractsSuccessfully() {
        val input = "instagram.com/reel/C8XYZ123abc/"
        val parsed = InstagramUrlParser.parse(input)
        assertNotNull(parsed)
        assertEquals("https://www.instagram.com/reel/C8XYZ123abc/", parsed?.canonicalUrl)
        assertEquals("C8XYZ123abc", parsed?.shortcode)
    }

    @Test
    fun extractAnyUrl_youtubeAndTiktok_extractsNormalized() {
        val yt = "youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals("https://youtube.com/watch?v=dQw4w9WgXcQ", InstagramUrlParser.extractAnyUrl(yt))

        val tiktok = "https://www.tiktok.com/@user/video/123456"
        assertEquals("https://www.tiktok.com/@user/video/123456", InstagramUrlParser.extractAnyUrl(tiktok))
    }

    @Test
    fun isUnsupportedUrl_detection() {
        org.junit.Assert.assertTrue(InstagramUrlParser.isUnsupportedUrl("https://www.youtube.com/watch?v=123"))
        org.junit.Assert.assertTrue(InstagramUrlParser.isUnsupportedUrl("youtube.com/shorts/123"))
        org.junit.Assert.assertTrue(InstagramUrlParser.isUnsupportedUrl("tiktok.com/@creator/123"))
        org.junit.Assert.assertFalse(InstagramUrlParser.isUnsupportedUrl("https://www.instagram.com/reel/C8XYZ123abc/"))
        org.junit.Assert.assertFalse(InstagramUrlParser.isUnsupportedUrl("instagram.com/reel/C8XYZ123abc/"))
        org.junit.Assert.assertFalse(InstagramUrlParser.isUnsupportedUrl("just random text"))
    }
}
