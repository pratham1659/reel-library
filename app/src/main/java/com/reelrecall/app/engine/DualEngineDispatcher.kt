package com.reelrecall.app.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.reelrecall.app.data.repository.AppPreferences
import com.reelrecall.app.data.repository.EngineMode
import com.reelrecall.app.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.reelrecall.app.data.model.ReelSummary
import java.io.File

class DualEngineDispatcher(
    private val context: Context
) {
    private val preferencesRepo = PreferencesRepository(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val onTokensUsed: (Long, Long, Long, String) -> Unit = { prompt, comp, latency, provider ->
        scope.launch {
            preferencesRepo.recordTokenUsage(
                promptTokens = prompt,
                completionTokens = comp,
                latencyMs = latency,
                statusCode = "200 OK",
                provider = provider
            )
        }
    }

    private val onWhisperAudioTranscribed: (Long, Long, Long) -> Unit = { audioSeconds, wordsTranscribed, latency ->
        scope.launch {
            preferencesRepo.recordTokenUsage(
                promptTokens = audioSeconds,
                completionTokens = wordsTranscribed,
                latencyMs = latency,
                statusCode = "200 OK",
                provider = "Groq LPU ~ Whisper",
                model = "whisper-large-v3-turbo",
                audioSeconds = audioSeconds
            )
        }
    }

    fun resolveTranscriptionEngine(prefs: AppPreferences): TranscriptionEngine {
        val whisperPath = prefs.whisperModelPath
        val whisperFile = if (!whisperPath.isNullOrBlank() && File(whisperPath).exists()) {
            File(whisperPath)
        } else {
            File(context.filesDir, "whisper-base.bin")
        }

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                if (prefs.groqApiKey.isNotBlank()) {
                    GroqWhisperEngine(prefs.groqApiKey, onAudioTranscribed = onWhisperAudioTranscribed)
                } else {
                    FailingTranscriptionEngine("Engine Mode is set to CLOUD_ONLY: Groq API key is missing. Local fallback is strictly disallowed.")
                }
            }

            EngineMode.LOCAL_ONLY -> {
                if (!whisperFile.exists() || whisperFile.length() < 10_000_000L) {
                    FailingTranscriptionEngine("Engine Mode is set to LOCAL_ONLY: On-device Whisper model weights not installed (${whisperFile.name}). Cloud fallback is strictly disallowed.")
                } else {
                    LocalWhisperEngine(whisperFile, onTokensUsed = onTokensUsed)
                }
            }

            EngineMode.AUTO_HYBRID -> {
                val isOnline = isNetworkAvailable()
                if (isOnline && prefs.groqApiKey.isNotBlank()) {
                    GroqWhisperEngine(prefs.groqApiKey, onAudioTranscribed = onWhisperAudioTranscribed)
                } else {
                    if (!whisperFile.exists() || whisperFile.length() < 10_000_000L) {
                        FailingTranscriptionEngine("Whisper transcription unavailable: Cloud requires a Groq API key and on-device Whisper weights are not installed.")
                    } else {
                        LocalWhisperEngine(whisperFile, onTokensUsed = onTokensUsed)
                    }
                }
            }
        }
    }

    fun resolveSummarizationEngine(prefs: AppPreferences): SummarizationEngine {
        val llmPath = prefs.llmModelPath
        val llmFile = if (!llmPath.isNullOrBlank() && File(llmPath).exists()) {
            File(llmPath)
        } else {
            File(context.filesDir, "gemma-2-2b-it-gpu.bin")
        }

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                if (prefs.groqApiKey.isNotBlank()) {
                    GroqSummarizerEngine(prefs.groqApiKey, onTokensUsed = onTokensUsed)
                } else if (prefs.geminiApiKey.isNotBlank()) {
                    GeminiSummarizerEngine(prefs.geminiApiKey, onTokensUsed = onTokensUsed)
                } else {
                    FailingSummarizerEngine("Engine Mode is set to CLOUD_ONLY: Cloud API key (Groq or Gemini) is missing. Local fallback is strictly disallowed.")
                }
            }

            EngineMode.LOCAL_ONLY -> {
                if (!llmFile.exists() || llmFile.length() < 50_000_000L) {
                    FailingSummarizerEngine("Engine Mode is set to LOCAL_ONLY: On-device MediaPipe LLM weights not installed (${llmFile.name}). Cloud fallback is strictly disallowed.")
                } else {
                    LocalMediaPipeSummarizerEngine(
                        context = context,
                        modelFile = llmFile,
                        onTokensUsed = onTokensUsed
                    )
                }
            }

            EngineMode.AUTO_HYBRID -> {
                val isOnline = isNetworkAvailable()
                if (isOnline && prefs.groqApiKey.isNotBlank()) {
                    GroqSummarizerEngine(prefs.groqApiKey, onTokensUsed = onTokensUsed)
                } else if (isOnline && prefs.geminiApiKey.isNotBlank()) {
                    GeminiSummarizerEngine(prefs.geminiApiKey, onTokensUsed = onTokensUsed)
                } else {
                    if (!llmFile.exists() || llmFile.length() < 50_000_000L) {
                        FailingSummarizerEngine("Summarization unavailable: Cloud API key missing and on-device LLM weights are not installed.")
                    } else {
                        LocalMediaPipeSummarizerEngine(
                            context = context,
                            modelFile = llmFile,
                            onTokensUsed = onTokensUsed
                        )
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class FailingTranscriptionEngine(private val reason: String) : TranscriptionEngine {
    override val name: String = "Transcription Engine Error"
    override suspend fun transcribe(audioFile: File): Result<String> {
        return Result.failure(IllegalStateException(reason))
    }
}

class FailingSummarizerEngine(private val reason: String) : SummarizationEngine {
    override val name: String = "Summarizer Engine Error"
    override suspend fun summarize(caption: String?, transcript: String): Result<ReelSummary> {
        return Result.failure(IllegalStateException(reason))
    }
}

