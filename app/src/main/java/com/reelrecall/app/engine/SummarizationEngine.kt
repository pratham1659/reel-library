package com.reelrecall.app.engine

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
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
                val hasSpokenTranscript = cleanTranscript.isNotBlank() && !cleanTranscript.equals("Not available", ignoreCase = true)

                val promptText = """
                    You are an expert executive research intelligence analyst. Deeply analyze this Instagram Reel and extract high-value insights.

                    PRIMARY SOURCES:
                    - Spoken Audio Transcript: ${if (hasSpokenTranscript) cleanTranscript else "Not available (No spoken voiceover detected; extract all insights from post caption)"}
                    - Post Caption: ${if (cleanCaption.isNotBlank()) cleanCaption else "None"}

                    INSTRUCTIONS:
                    1. If Spoken Audio Transcript is present and available, prioritize the actual spoken facts, mentioned platforms, websites, frameworks, numbers, or rules.
                    2. If Spoken Audio Transcript is absent or Not available, focus 100% on extracting all deep details, context, steps, and value directly from the Post Caption.
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
                val candidateModels = listOf("gemini-2.5-flash-preview-05-20", "gemini-2.0-flash", "gemini-1.5-flash-latest")
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

                val hasSpokenTranscript = cleanTranscript.isNotBlank() && !cleanTranscript.equals("Not available", ignoreCase = true)

                val systemPrompt = "You are an executive research analyst. Generate pure JSON for an Instagram Reel with keys: 'title' (specific, 4-7 words), 'one_line_summary' (detailed 2-3 sentence executive briefing), 'actionable_takeaways' (array of 4-6 granular bullet strings referencing tools/steps), and 'tags' (array of 3-5 specific strings without #)."
                val userContent = if (hasSpokenTranscript) {
                    """
                    PRIMARY SOURCE - SPOKEN AUDIO TRANSCRIPT:
                    $cleanTranscript

                    SECONDARY SOURCE - POST CAPTION:
                    ${if (cleanCaption.isNotBlank()) cleanCaption else "None"}

                    INSTRUCTIONS:
                    1. Focus primarily on what is spoken in the audio transcript. Generate the title, summary, and takeaways directly based on spoken words, tools, and instructions.
                    2. Cross-reference with the caption for links or spelling.
                    """.trimIndent()
                } else {
                    """
                    PRIMARY SOURCE - POST CAPTION (Spoken Audio Transcript is Not Available):
                    ${if (cleanCaption.isNotBlank()) cleanCaption else "Instagram Reel"}

                    INSTRUCTIONS:
                    1. Spoken audio transcript is NOT available for this reel.
                    2. Focus 100% on deeply analyzing the Post Caption. Extract all context, steps, workflows, tools, and takeaways directly from the caption.
                    3. Make the title and summary distinct and specific based on the caption text.
                    """.trimIndent()
                }

                val candidateModels = listOf("qwen/qwen3.8-27b", "llama-3.3-70b-versatile", "llama-3.1-8b-instant")
                var lastError: Exception? = null

                for (model in candidateModels) {
                    val payload = """
                        {
                          "model": "$model",
                          "messages": [
                            {"role": "system", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), systemPrompt)}},
                            {"role": "user", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), userContent)}}
                          ],
                          "max_tokens": 512,
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
                                        onTokensUsed?.invoke(promptTokens, completionTokens, latency, "Groq LPU ~ Qwen")
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
 * Local on-device summarization engine using MediaPipe Tasks GenAI (Gemma 2B / MediaPipe LLM tasks).
 * Executes strictly on-device using local model weights (.bin or .task).
 * If model weights are not downloaded or inference fails, it strictly fails without fallback.
 */
class LocalMediaPipeSummarizerEngine(
    private val context: Context,
    private val modelFile: File?,
    private val onTokensUsed: ((promptTokens: Long, completionTokens: Long, latencyMs: Long, provider: String) -> Unit)? = null
) : SummarizationEngine {

    override val name: String = "Local Gemma/Qwen (On-Device MediaPipe)"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> =
        withContext(Dispatchers.IO) {
            val targetFile = modelFile ?: File(context.filesDir, "gemma-2-2b-it-gpu.bin")

            // Real quantized LLM weights require at least 50MB (typically ~1.3GB to 1.5GB)
            if (!targetFile.exists() || targetFile.length() < 50_000_000L) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "On-device LLM model weights not installed on this device (${targetFile.name}). Engine Mode is set to LOCAL_ONLY; no cloud fallback allowed."
                    )
                )
            }

            // Clean input sources
            val cleanCaption = android.text.Html.fromHtml(caption ?: "", android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            val cleanTranscript = transcript.trim()
            val hasSpokenTranscript = cleanTranscript.isNotBlank() && !cleanTranscript.equals("Not available", ignoreCase = true)
            val content = (if (hasSpokenTranscript) cleanTranscript else cleanCaption).trim()

            if (content.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Cannot summarize reel: Spoken audio transcript and post caption are both empty.")
                )
            }

            val startTime = android.os.SystemClock.elapsedRealtime()

            try {
                // Initialize MediaPipe Tasks GenAI LlmInference
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(targetFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTemperature(0.2f)
                    .setTopK(40)
                    .build()

                val llmInference = LlmInference.createFromOptions(context, options)

                try {
                    val prompt = """
                        <start_of_turn>user
                        You are an executive research analyst. Deeply analyze this Instagram Reel and extract high-value insights.

                        Spoken Audio Transcript: ${if (hasSpokenTranscript) cleanTranscript else "Not available (Extract from post caption)"}
                        Post Caption: ${if (cleanCaption.isNotBlank()) cleanCaption else "None"}

                        Instructions:
                        1. ${if (hasSpokenTranscript) "Prioritize real tools, websites, and steps spoken in the audio transcript." else "Spoken transcript is not available. Extract all tools, websites, and steps directly from the Post Caption."}
                        2. Return pure valid JSON strictly matching this schema:
                        {
                          "title": "Distinct, punchy title (4 to 7 words)",
                          "one_line_summary": "Comprehensive 2-3 sentence executive briefing.",
                          "actionable_takeaways": [
                            "4 to 6 specific, practical, granular bullet points"
                          ],
                          "tags": [
                            "3 to 5 highly relevant topic strings without #"
                          ]
                        }
                        <end_of_turn>
                        <start_of_turn>model
                    """.trimIndent()

                    val promptTokens = try {
                        llmInference.sizeInTokens(prompt).toLong()
                    } catch (e: Exception) {
                        (prompt.length / 4L).coerceIn(16L, 1024L)
                    }

                    val rawResponse = llmInference.generateResponse(prompt)
                    val latency = android.os.SystemClock.elapsedRealtime() - startTime

                    if (rawResponse.isBlank()) {
                        return@withContext Result.failure(
                            IllegalStateException("Local MediaPipe model returned empty output.")
                        )
                    }

                    val cleanedJson = rawResponse.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val summary = try {
                        json.decodeFromString<ReelSummary>(cleanedJson)
                    } catch (e: Exception) {
                        // Extract JSON block between first '{' and last '}'
                        val startIdx = cleanedJson.indexOf('{')
                        val endIdx = cleanedJson.lastIndexOf('}')
                        if (startIdx != -1 && endIdx > startIdx) {
                            val candidate = cleanedJson.substring(startIdx, endIdx + 1)
                            json.decodeFromString<ReelSummary>(candidate)
                        } else {
                            // If model output natural text instead of JSON, parse safely
                            val lines = cleanedJson.lines().filter { it.isNotBlank() }
                            val fallbackTitle = lines.firstOrNull()?.take(60) ?: cleanCaption.take(50).ifBlank { "Local AI Summary" }
                            val fallbackSummary = lines.drop(1).take(3).joinToString(" ").ifBlank { cleanedJson.take(200) }
                            ReelSummary(
                                title = fallbackTitle,
                                one_line_summary = fallbackSummary,
                                actionable_takeaways = lines.filter { it.startsWith("-") || it.startsWith("*") || it.matches(Regex("""^\d+\..*""")) }.take(5).ifEmpty { listOf(fallbackSummary) },
                                tags = listOf("OnDeviceAI", "MediaPipe", "Offline")
                            )
                        }
                    }

                    val compTokens = (rawResponse.length / 4L).coerceAtLeast(10L)
                    onTokensUsed?.invoke(promptTokens, compTokens, latency, "On-Device MediaPipe (${targetFile.nameWithoutExtension})")

                    Result.success(summary)
                } finally {
                    try {
                        llmInference.close()
                    } catch (_: Exception) {}
                }
            } catch (e: Throwable) {
                Result.failure(
                    IllegalStateException(
                        "Local MediaPipe inference execution failed: ${e.message ?: e.javaClass.simpleName}. Engine Mode is LOCAL_ONLY; no cloud fallback allowed.",
                        e
                    )
                )
            }
        }
}

class MockSummarizationEngine : SummarizationEngine {
    override val name: String = "No-Op Summarizer"

    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> {
        return Result.failure(Exception("Sync failed: AI API key not configured or unavailable."))
    }
}


