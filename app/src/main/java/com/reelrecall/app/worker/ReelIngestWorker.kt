package com.reelrecall.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.repository.PreferencesRepository
import com.reelrecall.app.engine.DualEngineDispatcher
import com.reelrecall.app.engine.MockSummarizationEngine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ReelIngestWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDatabase.getInstance(appContext)
    private val preferencesRepository = PreferencesRepository(appContext)
    private val dispatcher = DualEngineDispatcher(appContext)

    override suspend fun doWork(): Result {
        val reelId = inputData.getLong(KEY_REEL_ID, -1L)
        val canonicalUrl = inputData.getString(KEY_CANONICAL_URL) ?: return Result.failure()

        if (reelId == -1L) return Result.failure()

        val dao = db.reelDao()

        try {
            // Step 1: Update status to EXTRACTING
            dao.updateStatus(reelId, IngestionStatus.EXTRACTING)

            val extractionResult = InstagramExtractor.extract(canonicalUrl, applicationContext.cacheDir)
            val metadata = extractionResult.getOrNull()
                ?: return handleError(reelId, "Failed to extract Instagram metadata: ${extractionResult.exceptionOrNull()?.message}")

            // Step 2: Update status to TRANSCRIBING
            dao.updateStatus(reelId, IngestionStatus.TRANSCRIBING)

            val currentPrefs = preferencesRepository.preferencesFlow.first()
            val transcriptionEngine = dispatcher.resolveTranscriptionEngine(currentPrefs)

            val transcript = if (metadata.audioFile != null && metadata.audioFile.exists() && metadata.audioFile.length() > 0) {
                transcriptionEngine.transcribe(metadata.audioFile).getOrNull()?.trim()?.ifBlank { null }
            } else {
                null
            }

            // Step 3: Update status to SUMMARIZING
            dao.updateStatus(reelId, IngestionStatus.SUMMARIZING)

            val summarizationEngine = dispatcher.resolveSummarizationEngine(currentPrefs)
            val summaryResult = summarizationEngine.summarize(metadata.caption, transcript ?: "")

            val summary = summaryResult.getOrElse { originalErr ->
                // Try Gemini if available and not yet tried
                val geminiFallback = if (currentPrefs.geminiApiKey.isNotBlank() && summarizationEngine !is com.reelrecall.app.engine.GeminiSummarizerEngine) {
                    com.reelrecall.app.engine.GeminiSummarizerEngine(currentPrefs.geminiApiKey).summarize(metadata.caption, transcript ?: "").getOrNull()
                } else null

                // Try Groq if available and not yet tried
                val groqFallback = if (geminiFallback == null && currentPrefs.groqApiKey.isNotBlank() && summarizationEngine !is com.reelrecall.app.engine.GroqSummarizerEngine) {
                    com.reelrecall.app.engine.GroqSummarizerEngine(currentPrefs.groqApiKey).summarize(metadata.caption, transcript ?: "").getOrNull()
                } else null

                geminiFallback
                    ?: groqFallback
                    ?: return handleError(reelId, "Sync failed: ${originalErr.message}")
            }

            val warning = summaryResult.exceptionOrNull()?.let { "(Fallback: ${it.message})" }

            // Step 4: Persist finalized entity in Room DB
            val existing = dao.findByCanonicalUrl(canonicalUrl)
            if (existing != null) {
                val cleanHandle = metadata.creatorHandle?.let {
                    android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                }
                val cleanCaption = metadata.caption?.let {
                    android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                }

                val updated = existing.copy(
                    creatorHandle = cleanHandle ?: existing.creatorHandle,
                    caption = cleanCaption ?: existing.caption,
                    audioPath = metadata.audioFile?.absolutePath,
                    thumbnailUrl = metadata.thumbnailUrl ?: existing.thumbnailUrl,
                    transcript = transcript,
                    title = summary.title,
                    oneLineSummary = summary.one_line_summary,
                    takeawaysJson = Json.encodeToString(summary.actionable_takeaways),
                    tagsJson = Json.encodeToString(summary.tags),
                    engineUsed = "${transcriptionEngine.name} + ${if (warning != null) "Fallback Engine" else summarizationEngine.name}",
                    status = IngestionStatus.COMPLETED,
                    errorMessage = warning
                )
                dao.update(updated)
            }


            postCompletionNotification(summary.title, summary.one_line_summary)

            return Result.success()
        } catch (e: Exception) {
            return handleError(reelId, e.message ?: "Unknown ingestion error")
        }
    }

    private suspend fun handleError(reelId: Long, error: String): Result {
        db.reelDao().updateStatus(reelId, IngestionStatus.FAILED, error)
        return Result.failure()
    }

    private fun postCompletionNotification(title: String, summary: String) {
        val channelId = "reel_recall_ingestion"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reel Ingestion Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a shared Instagram Reel has been summarized"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reel Recalled: $title")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_REEL_ID = "key_reel_id"
        const val KEY_CANONICAL_URL = "key_canonical_url"
    }
}
