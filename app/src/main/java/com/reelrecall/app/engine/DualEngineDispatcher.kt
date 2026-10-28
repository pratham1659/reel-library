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

    fun resolveTranscriptionEngine(prefs: AppPreferences): TranscriptionEngine {
        val isOnline = isNetworkAvailable()

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                if (prefs.groqApiKey.isNotBlank()) {
                    GroqWhisperEngine(prefs.groqApiKey)
                } else {
                    FailingTranscriptionEngine("Cloud transcription requires a Groq API key.")
                }
            }

            EngineMode.LOCAL_ONLY -> {
                val modelFile = prefs.whisperModelPath?.let { File(it) }
                if (prefs.isWhisperModelDownloaded && modelFile?.exists() == true) {
                    LocalWhisperEngine(modelFile)
                } else {
                    FailingTranscriptionEngine("Local Whisper model is not downloaded. Please download Whisper Base in Settings.")
                }
            }

            EngineMode.AUTO_HYBRID -> {
                if (isOnline && prefs.groqApiKey.isNotBlank()) {
                    GroqWhisperEngine(prefs.groqApiKey)
                } else {
                    val modelFile = prefs.whisperModelPath?.let { File(it) }
                    if (prefs.isWhisperModelDownloaded && modelFile?.exists() == true) {
                        LocalWhisperEngine(modelFile)
                    } else {
                        MockTranscriptionEngine()
                    }
                }
            }
        }
    }

    fun resolveSummarizationEngine(prefs: AppPreferences): SummarizationEngine {
        val isOnline = isNetworkAvailable()

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                if (prefs.geminiApiKey.isNotBlank()) {
                    GeminiSummarizerEngine(prefs.geminiApiKey, onTokensUsed = onTokensUsed)
                } else if (prefs.groqApiKey.isNotBlank()) {
                    GroqSummarizerEngine(prefs.groqApiKey, onTokensUsed = onTokensUsed)
                } else {
                    FailingSummarizerEngine("Cloud API keys missing. Please configure Groq or Gemini API key in Settings.")
                }
            }

            EngineMode.LOCAL_ONLY -> {
                val modelFile = prefs.llmModelPath?.let { File(it) }
                if (prefs.isLlmModelDownloaded && modelFile?.exists() == true) {
                    LocalMediaPipeSummarizerEngine(modelFile)
                } else {
                    FailingSummarizerEngine("Local LLM model is not downloaded. Please download Gemma 2B in Settings.")
                }
            }

            EngineMode.AUTO_HYBRID -> {
                if (isOnline && prefs.geminiApiKey.isNotBlank()) {
                    GeminiSummarizerEngine(prefs.geminiApiKey, onTokensUsed = onTokensUsed)
                } else if (isOnline && prefs.groqApiKey.isNotBlank()) {
                    GroqSummarizerEngine(prefs.groqApiKey, onTokensUsed = onTokensUsed)
                } else {
                    val modelFile = prefs.llmModelPath?.let { File(it) }
                    if (prefs.isLlmModelDownloaded && modelFile?.exists() == true) {
                        LocalMediaPipeSummarizerEngine(modelFile)
                    } else {
                        MockSummarizationEngine()
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

