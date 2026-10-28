package com.reelrecall.app.worker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object InstagramExtractor {

    private val client = OkHttpClient()

    data class ExtractedMetadata(
        val caption: String?,
        val creatorHandle: String?,
        val videoUrl: String?,
        val thumbnailUrl: String?,
        val audioFile: File? = null
    )

    private val OG_TITLE_REGEX = """<meta property="og:title" content="([^"]*)"\s*/?>""".toRegex()
    private val OG_DESC_REGEX = """<meta property="og:description" content="([^"]*)"\s*/?>""".toRegex()
    private val OG_VIDEO_REGEX = """<meta property="og:video" content="([^"]*)"\s*/?>""".toRegex()
    private val OG_IMAGE_REGEX = """<meta property="og:image" content="([^"]*)"\s*/?>""".toRegex()
    private val TWITTER_IMAGE_REGEX = """<meta name="twitter:image" content="([^"]*)"\s*/?>""".toRegex()
    private val TWITTER_TITLE_REGEX = """<meta name="twitter:title" content="([^"]*)"\s*/?>""".toRegex()
    private val KEYWORDS_REGEX = """<meta name="keywords" content="([^"]*)"\s*/?>""".toRegex()

    suspend fun extract(
        canonicalUrl: String,
        cacheDir: File
    ): Result<ExtractedMetadata> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(canonicalUrl)
                .addHeader(
                    "User-Agent",
                    "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"
                )
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .build()

            var caption: String? = null
            var creator: String? = null
            var videoUrl: String? = null
            var thumbnailUrl: String? = null

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""

                    val descMatch = OG_DESC_REGEX.find(html)
                    val rawDesc = descMatch?.groupValues?.get(1)
                    if (rawDesc != null) {
                        val unescaped = android.text.Html.fromHtml(rawDesc, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        // Clean boilerplate like '13K likes, 10K comments - username on date: "Actual Caption"'
                        val cleanCaption = if (unescaped.contains(": \"")) {
                            unescaped.substringAfter(": \"").removeSuffix("\"").removeSuffix("\".").trim()
                        } else if (unescaped.contains(" - ") && unescaped.contains(": ")) {
                            unescaped.substringAfter(": ").trim()
                        } else {
                            unescaped
                        }
                        caption = cleanCaption
                    }

                    val imageMatch = OG_IMAGE_REGEX.find(html) ?: TWITTER_IMAGE_REGEX.find(html)
                    thumbnailUrl = imageMatch?.groupValues?.get(1)?.replace("&amp;", "&")

                    val twitterTitleMatch = TWITTER_TITLE_REGEX.find(html)
                    val rawTwitterTitle = twitterTitleMatch?.groupValues?.get(1)
                    if (rawTwitterTitle != null) {
                        val cleaned = android.text.Html.fromHtml(rawTwitterTitle, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        val handle = Regex("(@[a-zA-Z0-9._]+)").find(cleaned)?.value
                        creator = handle ?: cleaned.substringBefore(" •").trim()
                    }

                    if (creator == null) {
                        val titleMatch = OG_TITLE_REGEX.find(html)
                        val rawTitle = titleMatch?.groupValues?.get(1)
                        if (rawTitle != null && rawTitle.contains("on Instagram:")) {
                            creator = rawTitle.substringBefore("on Instagram:").trim()
                        }
                    }

                    if (creator != null) {
                        val handleMatch = Regex("(@[a-zA-Z0-9._]+)").find(creator!!)?.value
                        if (handleMatch != null) creator = handleMatch
                    }

                    val keywordsMatch = KEYWORDS_REGEX.find(html)
                    val keywords = keywordsMatch?.groupValues?.get(1)
                    if (caption.isNullOrBlank() && !keywords.isNullOrBlank()) {
                        caption = keywords
                    }

                    val videoMatch = OG_VIDEO_REGEX.find(html)
                    videoUrl = videoMatch?.groupValues?.get(1)?.replace("&amp;", "&")
                }
            }

            // Step 1b: If direct videoUrl was not found in OG tags, query Instagram /embed/captioned/ endpoint
            if (videoUrl.isNullOrBlank()) {
                try {
                    val embedUrl = "${canonicalUrl.trimEnd('/')}/embed/captioned/"
                    val embedRequest = Request.Builder()
                        .url(embedUrl)
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"
                        )
                        .addHeader("Accept-Language", "en-US,en;q=0.9")
                        .build()

                    client.newCall(embedRequest).execute().use { embedRes ->
                        if (embedRes.isSuccessful) {
                            val embedHtml = embedRes.body?.string() ?: ""
                            val match = Regex("""video_url[^\"]*?[\\:\"]+(https:[^\"]+?\.mp4[^\"]*)""").find(embedHtml)
                            if (match != null) {
                                val rawMatched = match.groupValues[1]
                                val unescaped = rawMatched
                                    .replace("\\/", "/")
                                    .replace("\\u0025", "%")
                                    .replace("\\u0026", "&")
                                    .replace("&amp;", "&")
                                    .replace("\\", "")
                                videoUrl = unescaped
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("InstagramExtractor", "Embed scraping fallback error", e)
                }
            }

            // Download media stream if direct videoUrl is available
            var downloadedFile: File? = null
            if (!videoUrl.isNullOrBlank()) {
                try {
                    val mediaRequest = Request.Builder()
                        .url(videoUrl!!)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build()

                    client.newCall(mediaRequest).execute().use { res ->
                        if (res.isSuccessful && res.body != null) {
                            val tempFile = File(cacheDir, "reel_audio_${System.currentTimeMillis()}.mp4")
                            FileOutputStream(tempFile).use { out ->
                                res.body!!.byteStream().copyTo(out)
                            }
                            if (tempFile.exists() && tempFile.length() > 0) {
                                downloadedFile = tempFile
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("InstagramExtractor", "Failed downloading MP4 video", e)
                }
            }

            Result.success(
                ExtractedMetadata(
                    caption = caption?.ifBlank { null } ?: "Instagram Reel shared to Reel Library.",
                    creatorHandle = creator ?: "@instagram_creator",
                    videoUrl = videoUrl,
                    thumbnailUrl = thumbnailUrl,
                    audioFile = downloadedFile
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
