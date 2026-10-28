package com.reelrecall.app.engine

import com.reelrecall.app.data.model.ReelSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

interface SummarizationEngine {
    val name: String
    suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary>
}

@Serializable
private data class GeminiPart(val text: String)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiCandidate(val content: GeminiContent)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

/**
 * Cloud Summarizer using Google AI Studio Gemini API (gemini-3.5-flash-lite / gemini-2.5-flash).
 */
class GeminiSummarizerEngine(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val onTokensUsed: ((promptTokens: Long, completionTokens: Long, latencyMs: Long, provider: String) -> Unit)? = null
) : SummarizationEngine {

    override val name: String = "Google Gemini (Cloud AI)"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> =
        withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Gemini API key is not configured in Settings.")
                    )
                }

                val cleanCaption = android.text.Html.fromHtml(caption ?: "", android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                val cleanTranscript = transcript.trim()

                val promptText = """
                    You are an expert executive research intelligence analyst. Deeply analyze this Instagram Reel and extract high-value insights.

                    PRIMARY SOURCES:
                    - Spoken Audio Transcript: ${if (cleanTranscript.isNotBlank()) cleanTranscript else "None (No spoken voiceover detected; extract all insights from post caption)"}
                    - Post Caption: ${if (cleanCaption.isNotBlank()) cleanCaption else "None"}

                    INSTRUCTIONS:
                    1. If Spoken Audio Transcript is present, prioritize the actual spoken facts, mentioned platforms, websites, frameworks, numbers, or rules.
                    2. If Spoken Audio Transcript is absent, extract all deep details, context, steps, and value directly from the Post Caption.
                    3. Do NOT produce generic, vague, or placeholder text (never say "Resource List" or "Save Time And Money"). Make every summary distinct and specific to what this reel actually teaches.
                    4. Always return valid, well-formed JSON matching this schema:
                    {
                      "title": "Distinct, punchy, specific title (4 to 7 words, e.g. '10 Startup Grants & Cloud Credits')",
                      "one_line_summary": "Comprehensive 2-3 sentence executive summary explaining the core hook/problem, exact tools/frameworks mentioned, and practical benefits.",
                      "actionable_takeaways": [
                        "4 to 6 specific, practical, granular bullet points quoting exact tool names, websites, prompts, or actionable steps from the content"
                      ],
                      "tags": [
                        "3 to 5 highly relevant, specific hashtag category topics matching the exact subject without # symbol (e.g. 'StartupFunding', 'CloudCredits', 'ClaudeAI', 'SaaS')"
                      ]
                    }
                """.trimIndent()

                val requestPayload = """
                    {
                      "contents": [{
                        "parts": [{"text": ${Json.encodeToString(kotlinx.serialization.serializer(), promptText)}}]
                      }],
                      "generationConfig": {
                        "responseMimeType": "application/json"
                      }
                    }
                """.trimIndent()

                val cleanKey = apiKey.trim()
                val candidateModels = listOf("gemini-3.5-flash-lite", "gemini-flash-latest", "gemini-2.5-flash")
                var lastError: Exception? = null

                for (model in candidateModels) {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
                    val body = requestPayload.toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-goog-api-key", cleanKey)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()

                    val startTime = android.os.SystemClock.elapsedRealtime()
                    try {
                        client.newCall(request).execute().use { response ->
                            val latency = android.os.SystemClock.elapsedRealtime() - startTime
                            if (response.isSuccessful) {
                                val responseString = response.body?.string() ?: ""
                                try {
                                    val rootObj = json.decodeFromString<JsonObject>(responseString)
                                    val usage = rootObj["usageMetadata"] as? JsonObject
                                    val promptTokens = usage?.get("promptTokenCount")?.jsonPrimitive?.longOrNull ?: 0L
                                    val candTokens = usage?.get("candidatesTokenCount")?.jsonPrimitive?.longOrNull ?: 0L
                                    val totalTokens = usage?.get("totalTokenCount")?.jsonPrimitive?.longOrNull ?: (promptTokens + candTokens)
                                    val completionTokens = if (candTokens > 0) candTokens else (totalTokens - promptTokens).coerceAtLeast(0L)
                                    if (promptTokens > 0 || completionTokens > 0) {
                                        onTokensUsed?.invoke(promptTokens, completionTokens, latency, "Gemini ($model)")
                                    }
                                } catch (e: Exception) {
                                    // Non-fatal telemetry parsing
                                }

                                val geminiResp = json.decodeFromString<GeminiResponse>(responseString)
                                val rawJsonText = geminiResp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                if (!rawJsonText.isNullOrBlank()) {
                                    val cleanedJson = rawJsonText.trim()
                                        .removePrefix("```json")
                                        .removePrefix("```")
                                        .removeSuffix("```")
                                        .trim()
                                    val summary = json.decodeFromString<ReelSummary>(cleanedJson)
                                    return@withContext Result.success(summary)
                                }
                            } else {
                                val err = response.body?.string() ?: "Code ${response.code}"
                                lastError = Exception("Gemini ($model) failed (${response.code}): $err")
                            }
                        }
                    } catch (e: Exception) {
                        lastError = e
                    }
                }

                Result.failure(lastError ?: Exception("All Gemini models failed."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

/**
 * Cloud Summarizer using Groq's high-speed models (qwen/qwen3.8-27b / openai/gpt-oss-120b).
 */
class GroqSummarizerEngine(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val onTokensUsed: ((promptTokens: Long, completionTokens: Long, latencyMs: Long, provider: String) -> Unit)? = null
) : SummarizationEngine {

    override val name: String = "Groq LPU (High Speed Cloud)"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> =
        withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Groq API key is not configured."))
                }

                val cleanCaption = android.text.Html.fromHtml(caption ?: "", android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                val cleanTranscript = transcript.trim()

                val systemPrompt = "You are an executive research analyst. Generate pure JSON for an Instagram Reel with keys: 'title' (specific, 4-7 words), 'one_line_summary' (detailed 2-3 sentence executive briefing), 'actionable_takeaways' (array of 4-6 granular bullet strings referencing tools/steps), and 'tags' (array of 3-5 specific strings without #)."
                val userContent = """
                    Spoken Audio Transcript: ${if (cleanTranscript.isNotBlank()) cleanTranscript else "None (No spoken voiceover detected; extract all details from post caption)"}
                    Post Caption: ${if (cleanCaption.isNotBlank()) cleanCaption else "None"}
                    
                    Instructions: If Spoken Audio Transcript is present, prioritize actual spoken tools and advice. If absent, extract details thoroughly from Post Caption. Do not generate generic or matching duplicate content.
                """.trimIndent()

                val candidateModels = listOf("qwen/qwen3.8-27b", "openai/gpt-oss-120b", "openai/gpt-oss-20b")
                var lastError: Exception? = null

                for (model in candidateModels) {
                    val payload = """
                        {
                          "model": "$model",
                          "messages": [
                            {"role": "system", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), systemPrompt)}},
                            {"role": "user", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), userContent)}}
                          ],
                          "response_format": {"type": "json_object"}
                        }
                    """.trimIndent()

                    val body = payload.toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("https://api.groq.com/openai/v1/chat/completions")
                        .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                        .post(body)
                        .build()

                    val startTime = android.os.SystemClock.elapsedRealtime()
                    try {
                        client.newCall(request).execute().use { response ->
                            val latency = android.os.SystemClock.elapsedRealtime() - startTime
                            val respStr = response.body?.string() ?: ""
                            if (response.isSuccessful) {
                                val rootObj = json.decodeFromString<JsonObject>(respStr)
                                try {
                                    val usage = rootObj["usage"] as? JsonObject
                                    val promptTokens = usage?.get("prompt_tokens")?.jsonPrimitive?.longOrNull ?: 0L
                                    val completionTokens = usage?.get("completion_tokens")?.jsonPrimitive?.longOrNull ?: 0L
                                    if (promptTokens > 0 || completionTokens > 0) {
                                        onTokensUsed?.invoke(promptTokens, completionTokens, latency, "Groq ($model)")
                                    }
                                } catch (e: Exception) {
                                    // Non-fatal telemetry parsing
                                }

                                val contentStr = rootObj["choices"]
                                    ?.let { it as? kotlinx.serialization.json.JsonArray }
                                    ?.firstOrNull()
                                    ?.let { it as? JsonObject }
                                    ?.get("message")
                                    ?.let { it as? JsonObject }
                                    ?.get("content")
                                    ?.let { it as? JsonPrimitive }
                                    ?.content

                                if (!contentStr.isNullOrBlank()) {
                                    val cleanedJson = contentStr.trim()
                                        .removePrefix("```json")
                                        .removePrefix("```")
                                        .removeSuffix("```")
                                        .trim()
                                    val summary = json.decodeFromString<ReelSummary>(cleanedJson)
                                    return@withContext Result.success(summary)
                                }
                            } else {
                                lastError = Exception("Groq ($model) failed (${response.code}): $respStr")
                            }
                        }
                    } catch (e: Exception) {
                        lastError = e
                    }
                }

                Result.failure(lastError ?: Exception("All Groq models failed."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

/**
 * Local on-device summarization engine using MediaPipe GenAI / Qwen 2.5 / Gemma 2B.
 */
class LocalMediaPipeSummarizerEngine(
    private val modelFile: File?
) : SummarizationEngine {

    override val name: String = "Local Gemma/Qwen (On-Device NPU)"

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> =
        withContext(Dispatchers.Default) {
            if (modelFile == null || !modelFile.exists()) {
                return@withContext Result.failure(
                    IllegalStateException("Sync failed: Local LLM model file not found.")
                )
            }
            Result.failure(IllegalStateException("Sync failed: On-device model not initialized."))
        }
}

class MockSummarizationEngine : SummarizationEngine {
    override val name: String = "No-Op Summarizer"

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> {
        return Result.failure(Exception("Sync failed: AI API key not configured or unavailable."))
    }
}


