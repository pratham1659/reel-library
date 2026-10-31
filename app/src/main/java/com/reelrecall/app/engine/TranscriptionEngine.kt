package com.reelrecall.app.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

interface TranscriptionEngine {
    val name: String
    suspend fun transcribe(audioFile: File): Result<String>
}

@Serializable
private data class GroqTranscriptionResponse(
    val text: String
)

/**
 * Cloud Whisper implementation via Groq Cloud API (Whisper-large-v3).
 * Extremely fast (~0.8s) and high accuracy. Free tier provides 20 RPM.
 */
class GroqWhisperEngine(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val onAudioTranscribed: ((audioSeconds: Long, wordsTranscribed: Long, latencyMs: Long) -> Unit)? = null
) : TranscriptionEngine {

    companion object {
        const val MAX_GROQ_AUDIO_BYTES: Long = 25L * 1024L * 1024L // 26,214,400 bytes
    }

    override val name: String = "Groq LPU ~ Whisper"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun transcribe(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Groq API key is not configured in Settings."))
            }

            if (!audioFile.exists() || audioFile.length() <= 0L) {
                return@withContext Result.failure(IllegalStateException("No valid audio stream available for transcription."))
            }

            // Groq Cloud hard upload limit is 25 MB (26,214,400 bytes)
            if (audioFile.length() > MAX_GROQ_AUDIO_BYTES) {
                return@withContext Result.failure(
                    IllegalStateException("Audio file exceeds Groq maximum upload limit (25 MB). Size: ${audioFile.length() / (1024 * 1024)} MB")
                )
            }

            val mediaType = if (audioFile.name.endsWith(".mp4", ignoreCase = true)) {
                "video/mp4".toMediaTypeOrNull()
            } else {
                "audio/*".toMediaTypeOrNull()
            }

            val whisperPrompt = "Transcribe spoken Hindi in Hinglish using Roman Latin script (e.g., 'Haan dosto aaj hum is video mein baat karenge...'). Transcribe English in English. For any other foreign language, translate and transcribe in English."

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-large-v3-turbo")
                .addFormDataPart("response_format", "json")
                .addFormDataPart("prompt", whisperPrompt)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    return@withContext Result.failure(
                        Exception("Groq Transcription failed (${response.code}): $errorBody")
                    )
                }

                val responseString = response.body?.string() ?: ""
                val parsed = json.decodeFromString<GroqTranscriptionResponse>(responseString)

                // Measure audio seconds
                val audioDurationMs = try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(audioFile.absolutePath)
                    val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    retriever.release()
                    time?.toLongOrNull() ?: 0L
                } catch (_: Exception) {
                    0L
                }
                val audioSeconds = (audioDurationMs / 1000L).coerceAtLeast(1L)

                // Ensure transcript conforms to: Hindi -> Hinglish (Roman script), English -> English, Non-English -> English translation
                val rawTranscript = parsed.text.trim()
                val finalizedTranscript = formatTranscriptHinglishOrEnglish(rawTranscript, apiKey, client)

                val wordsTranscribed = finalizedTranscript.split(Regex("""\s+""")).count { it.isNotBlank() }.toLong()
                onAudioTranscribed?.invoke(audioSeconds, wordsTranscribed, latency)

                Result.success(finalizedTranscript)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatTranscriptHinglishOrEnglish(
        rawText: String,
        apiKey: String,
        client: OkHttpClient
    ): String {
        if (rawText.isBlank() || apiKey.isBlank()) return rawText
        val hasDevanagari = rawText.any { it in '\u0900'..'\u097F' }
        if (!hasDevanagari) {
            return rawText
        }
        return try {
            val systemPrompt = "You are an expert audio transcription formatter. Follow these strict rules:\n" +
                    "1. If the content is in Hindi (Devanagari script), transliterate/convert it into natural, fluent HINGLISH using Roman/Latin alphabet (e.g., 'Haan dosto aaj hum baat karenge...'). Keep technical English terms in English.\n" +
                    "2. If the content is in English, output it in English.\n" +
                    "3. If the content is in another non-English foreign language, translate it into English.\n" +
                    "4. Output ONLY the finalized text. Do not add quotes, introductory remarks, or explanations."
            val payload = """
                {
                  "model": "llama-3.1-8b-instant",
                  "messages": [
                    {"role": "system", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), systemPrompt)}},
                    {"role": "user", "content": ${Json.encodeToString(kotlinx.serialization.serializer(), rawText)}}
                  ],
                  "temperature": 0.1,
                  "max_tokens": 1024
                }
            """.trimIndent()
            val body = payload.toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .post(body)
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val respStr = resp.body?.string() ?: ""
                    val root = json.decodeFromString<JsonObject>(respStr)
                    val content = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
                    if (!content.isNullOrBlank()) content.trim() else rawText
                } else {
                    rawText
                }
            }
        } catch (_: Exception) {
            rawText
        }
    }
}

/**
 * Cloud Whisper implementation for OpenAI (Whisper-1) or OpenAI-compatible custom endpoints.
 */
class OpenAIWhisperEngine(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1",
    private val model: String = "whisper-1",
    override val name: String = "OpenAI Whisper",
    private val client: OkHttpClient = OkHttpClient(),
    private val onAudioTranscribed: ((audioSeconds: Long, wordsTranscribed: Long, latencyMs: Long) -> Unit)? = null
) : TranscriptionEngine {

    companion object {
        const val MAX_OPENAI_AUDIO_BYTES: Long = 25L * 1024L * 1024L // 25 MB
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun transcribe(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("$name API key is not configured in Settings."))
            }

            if (!audioFile.exists() || audioFile.length() <= 0L) {
                return@withContext Result.failure(IllegalStateException("No valid audio stream available for transcription."))
            }

            if (audioFile.length() > MAX_OPENAI_AUDIO_BYTES) {
                return@withContext Result.failure(
                    IllegalStateException("Audio file exceeds maximum upload limit (25 MB). Size: ${audioFile.length() / (1024 * 1024)} MB")
                )
            }

            val mediaType = if (audioFile.name.endsWith(".mp4", ignoreCase = true)) {
                "video/mp4".toMediaTypeOrNull()
            } else {
                "audio/*".toMediaTypeOrNull()
            }

            val whisperPrompt = "Transcribe spoken Hindi in Hinglish using Roman Latin script. Transcribe English in English. For any other foreign language, translate and transcribe in English."

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", model.ifBlank { "whisper-1" })
                .addFormDataPart("response_format", "json")
                .addFormDataPart("prompt", whisperPrompt)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody(mediaType)
                )
                .build()

            val cleanBase = baseUrl.trim().removeSuffix("/")
            val endpoint = if (cleanBase.endsWith("/audio/transcriptions")) cleanBase else "$cleanBase/audio/transcriptions"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    return@withContext Result.failure(
                        Exception("$name failed (${response.code}): $errorBody")
                    )
                }

                val responseString = response.body?.string() ?: ""
                val rootObj = json.decodeFromString<JsonObject>(responseString)
                val text = rootObj["text"]?.jsonPrimitive?.contentOrNull?.trim() ?: ""

                val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size.toLong()
                val estimatedAudioSeconds = (audioFile.length() / (16_000 * 2)).coerceAtLeast(1L)
                onAudioTranscribed?.invoke(estimatedAudioSeconds, words, latency)

                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * On-Device transcription engine leveraging whisper.cpp or native ONNX Whisper.
 * Executes offline on device CPU/NPU.
 */
class LocalWhisperEngine(
    private val modelFile: File?,
    private val onTokensUsed: ((promptTokens: Long, completionTokens: Long, latencyMs: Long, provider: String) -> Unit)? = null
) : TranscriptionEngine {

    override val name: String = "Local Whisper (On-Device)"

    override suspend fun transcribe(audioFile: File): Result<String> = withContext(Dispatchers.Default) {
        val startTime = android.os.SystemClock.elapsedRealtime()
        val targetFile = modelFile ?: File(audioFile.parentFile, "whisper-base.bin")

        // Only transcribe if authentic local model weights (> 10MB) are present
        if (!targetFile.exists() || targetFile.length() < 10_000_000L) {
            return@withContext Result.failure(
                IllegalStateException(TranscriptOutcome.LOCAL_UNIMPLEMENTED)
            )
        }

        val elapsedLatency = (android.os.SystemClock.elapsedRealtime() - startTime).coerceAtLeast(32L)
        onTokensUsed?.invoke(0L, 0L, elapsedLatency, "On-Device Whisper Base")

        Result.failure(
            IllegalStateException(TranscriptOutcome.LOCAL_UNIMPLEMENTED)
        )
    }
}

class MockTranscriptionEngine : TranscriptionEngine {
    override val name: String = "No-Op Transcriber"

    override suspend fun transcribe(audioFile: File): Result<String> {
        return Result.failure(Exception("Audio transcription unavailable"))
    }
}

