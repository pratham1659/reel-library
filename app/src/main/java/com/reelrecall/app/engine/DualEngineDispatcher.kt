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

        fun makeCloudTranscriptionEngine(): TranscriptionEngine? {
            return when (prefs.selectedProvider) {
                com.reelrecall.app.data.repository.AIProvider.GROQ -> {
                    if (prefs.groqApiKey.isNotBlank()) GroqWhisperEngine(prefs.groqApiKey, onAudioTranscribed = onWhisperAudioTranscribed) else null
                }
                com.reelrecall.app.data.repository.AIProvider.OPENAI -> {
                    if (prefs.openAIApiKey.isNotBlank()) OpenAIWhisperEngine(prefs.openAIApiKey, model = "whisper-1", onAudioTranscribed = onWhisperAudioTranscribed) else null
                }
                com.reelrecall.app.data.repository.AIProvider.CLAUDE -> {
                    if (prefs.openAIApiKey.isNotBlank()) {
                        OpenAIWhisperEngine(prefs.openAIApiKey, model = "whisper-1", name = "OpenAI Whisper (Claude Hybrid)", onAudioTranscribed = onWhisperAudioTranscribed)
                    } else if (prefs.groqApiKey.isNotBlank()) {
                        GroqWhisperEngine(prefs.groqApiKey, onAudioTranscribed = onWhisperAudioTranscribed)
                    } else null
                }
                com.reelrecall.app.data.repository.AIProvider.CUSTOM -> {
                    if (prefs.customApiKey.isNotBlank() && prefs.customTranscriptionModel.isNotBlank()) {
                        OpenAIWhisperEngine(prefs.customApiKey, baseUrl = prefs.customBaseUrl, model = prefs.customTranscriptionModel, name = "Custom Whisper", onAudioTranscribed = onWhisperAudioTranscribed)
                    } else if (prefs.openAIApiKey.isNotBlank()) {
                        OpenAIWhisperEngine(prefs.openAIApiKey, model = "whisper-1", onAudioTranscribed = onWhisperAudioTranscribed)
                    } else if (prefs.groqApiKey.isNotBlank()) {
                        GroqWhisperEngine(prefs.groqApiKey, onAudioTranscribed = onWhisperAudioTranscribed)
                    } else null
                }
            }
        }

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                val cloudEngine = makeCloudTranscriptionEngine()
                if (cloudEngine != null) {
                    cloudEngine
                } else {
                    val msg = if (prefs.selectedProvider == com.reelrecall.app.data.repository.AIProvider.CLAUDE) {
                        "Engine Mode is set to CLOUD_ONLY with Claude: Claude lacks an audio API. Please add an OpenAI or Groq API key in Settings for transcription."
                    } else {
                        "Engine Mode is set to CLOUD_ONLY: ${prefs.selectedProvider.displayName} API key is missing. Local fallback is strictly disallowed."
                    }
                    FailingTranscriptionEngine(msg)
                }
            }

            EngineMode.LOCAL_ONLY -> {
                if (!whisperFile.exists() || whisperFile.length() < 10_000_000L) {
                    FailingTranscriptionEngine("Engine Mode is set to LOCAL_ONLY: ${TranscriptOutcome.LOCAL_UNIMPLEMENTED}")
                } else {
                    LocalWhisperEngine(whisperFile, onTokensUsed = onTokensUsed)
                }
            }

            EngineMode.AUTO_HYBRID -> {
                val isOnline = isNetworkAvailable()
                val cloudEngine = if (isOnline) makeCloudTranscriptionEngine() else null
                if (cloudEngine != null) {
                    cloudEngine
                } else {
                    if (!whisperFile.exists() || whisperFile.length() < 10_000_000L) {
                        FailingTranscriptionEngine("Whisper transcription unavailable: ${prefs.selectedProvider.displayName} requires an API key (or active internet) and on-device Whisper weights are not installed.")
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

        fun makeCloudSummarizerEngine(): SummarizationEngine? {
            return when (prefs.selectedProvider) {
                com.reelrecall.app.data.repository.AIProvider.GROQ -> {
                    if (prefs.groqApiKey.isNotBlank()) GroqSummarizerEngine(prefs.groqApiKey, onTokensUsed = onTokensUsed) else null
                }
                com.reelrecall.app.data.repository.AIProvider.OPENAI -> {
                    if (prefs.openAIApiKey.isNotBlank()) OpenAISummarizerEngine(prefs.openAIApiKey, candidateModels = listOf("gpt-4o-mini", "gpt-4o"), name = "OpenAI", onTokensUsed = onTokensUsed) else null
                }
                com.reelrecall.app.data.repository.AIProvider.CLAUDE -> {
                    if (prefs.claudeApiKey.isNotBlank()) AnthropicClaudeSummarizerEngine(prefs.claudeApiKey, onTokensUsed = onTokensUsed) else null
                }
                com.reelrecall.app.data.repository.AIProvider.CUSTOM -> {
                    val model = prefs.customSummarizerModel.ifBlank { "deepseek/deepseek-chat" }
                    if (prefs.customApiKey.isNotBlank()) {
                        OpenAISummarizerEngine(
                            apiKey = prefs.customApiKey,
                            baseUrl = prefs.customBaseUrl,
                            candidateModels = listOf(model),
                            name = "Custom ($model)",
                            onTokensUsed = onTokensUsed
                        )
                    } else null
                }
            }
        }

        return when (prefs.engineMode) {
            EngineMode.CLOUD_ONLY -> {
                val cloudEngine = makeCloudSummarizerEngine()
                if (cloudEngine != null) {
                    cloudEngine
                } else {
                    FailingSummarizerEngine("Engine Mode is set to CLOUD_ONLY: ${prefs.selectedProvider.displayName} API key is missing. Local fallback is strictly disallowed.")
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
                val cloudEngine = if (isOnline) makeCloudSummarizerEngine() else null
                if (cloudEngine != null) {
                    cloudEngine
                } else {
                    if (!llmFile.exists() || llmFile.length() < 50_000_000L) {
                        FailingSummarizerEngine("Summarization unavailable: ${prefs.selectedProvider.displayName} API key missing and on-device LLM weights are not installed.")
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

class FailingTranscriptionEngine(val reason: String) : TranscriptionEngine {
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

