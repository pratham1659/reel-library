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
import com.reelrecall.app.engine.GroqWhisperEngine
import com.reelrecall.app.engine.MockSummarizationEngine
import com.reelrecall.app.engine.OpenAIWhisperEngine
import com.reelrecall.app.engine.TranscriptOutcome
import com.reelrecall.app.engine.Transcripts
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
            val transcriptOutcome = transcribeExtracted(metadata, transcriptionEngine)
            val transcript = transcriptOutcome.summaryInput

            // Check reel duration: if > 1 minute (60,000ms), extract all details & transcript
            // but skip AI token summarization until user explicitly runs it from the Reel Detail page.
            val durationMs = if (metadata.audioFile != null && metadata.audioFile.exists() && metadata.audioFile.length() > 0) {
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(metadata.audioFile.absolutePath)
                    val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    retriever.release()
                    time?.toLongOrNull() ?: 0L
                } catch (e: Exception) {
                    0L
                }
            } else {
                0L
            }

            val existing = dao.findByCanonicalUrl(canonicalUrl)
            val cleanHandle = metadata.creatorHandle?.let {
                android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            }
            val cleanCaption = metadata.caption?.let {
                android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            }

            val isExportNotSupported = metadata.isRestricted || metadata.videoUrl.isNullOrBlank() || metadata.audioFile == null

            if (isExportNotSupported) {
                // RULE: If reel media export is not supported by Instagram, DO NOT run AI to waste API quota.
                // Mark as NOT_SUPPORTED in status chips and present clean native details.
                if (existing != null) {
                    val shortcode = existing.shortcode
                    val handleDisplay = cleanHandle?.takeIf { it.isNotBlank() && it != "@instagram_creator" }
                    
                    val sanitizedCaption = cleanCaption
                        ?.replace(Regex("""(?i)^(comment|type|drop|dm|send)\s+["']?[a-zA-Z0-9_-]+["']?\s+(and|to|for|&).*?([.!?\n]|$)"""), "")
                        ?.trim()
                    val realTitle = sanitizedCaption?.take(60)?.ifBlank { null }
                        ?: cleanCaption?.take(60)?.ifBlank { null }
                        ?: (if (handleDisplay != null) "Instagram Reel by $handleDisplay" else "Instagram Reel #$shortcode")

                    val summary = cleanCaption?.take(180)?.ifBlank { null }
                        ?: "Video export is not supported by Instagram for this reel. Audio transcription and AI summary were skipped to save your API quota."

                    val updated = existing.copy(
                        creatorHandle = handleDisplay ?: existing.creatorHandle,
                        caption = cleanCaption ?: existing.caption,
                        audioPath = null,
                        thumbnailUrl = metadata.thumbnailUrl ?: existing.thumbnailUrl,
                        transcript = null,
                        transcriptNote = metadata.audioNote ?: "Instagram does not permit external video export for this reel. Tap 'Watch Reel on Instagram' to view.",
                        title = realTitle,
                        oneLineSummary = summary,
                        takeawaysJson = "[]",
                        tagsJson = "[]",
                        engineUsed = "Export Not Supported (AI Skipped)",
                        status = IngestionStatus.NOT_SUPPORTED,
                        errorMessage = null
                    )
                    dao.update(updated)
                }

                val notifTitle = cleanCaption?.take(40) ?: "Reel Library: Export Not Supported"
                postCompletionNotification(notifTitle, "Export not supported for this reel. Tap in app to view on Instagram.")
                return Result.success()
            }

            val shouldSkipAiForDuration = durationMs > 60_000L && currentPrefs.engineMode == com.reelrecall.app.data.repository.EngineMode.CLOUD_ONLY

            if (shouldSkipAiForDuration) {
                // RULE: Long Reel (> 1 min) in Cloud Mode -> Save all extracted details without consuming paid/free API rate limits
                if (existing != null) {
                    val sanitizedCaption = cleanCaption?.replace(Regex("""(?i)^(comment|type|drop|dm|send)\s+["']?[a-zA-Z0-9_-]+["']?\s+(and|to|for|&).*?([.!?\n]|$)"""), "")?.trim()
                    val fallbackTitle = sanitizedCaption?.take(60)?.ifBlank { null }
                        ?: cleanCaption?.take(60)?.ifBlank { null }
                        ?: "Instagram Reel by ${cleanHandle ?: "@creator"}"
                    val updated = existing.copy(
                        creatorHandle = cleanHandle ?: existing.creatorHandle,
                        caption = cleanCaption ?: existing.caption,
                        audioPath = metadata.audioFile?.absolutePath,
                        thumbnailUrl = metadata.thumbnailUrl ?: existing.thumbnailUrl,
                        transcript = transcriptOutcome.text,
                        transcriptNote = transcriptOutcome.note,
                        title = fallbackTitle,
                        oneLineSummary = null,
                        takeawaysJson = "[]",
                        tagsJson = "[]",
                        engineUsed = transcriptionEngine.name,
                        status = IngestionStatus.COMPLETED,
                        errorMessage = null
                    )
                    dao.update(updated)
                }

                val notifTitle = cleanCaption?.take(40) ?: "Reel Library: Long Reel Indexed"
                postCompletionNotification(notifTitle, "Reel is over 1 min. Tap in app to run AI summary on demand.")
                return Result.success()
            }

            // Step 3: Update status to SUMMARIZING for reels <= 1 minute
            dao.updateStatus(reelId, IngestionStatus.SUMMARIZING)

            // Keep the transcript even if summarization later fails.
            if (existing != null) {
                dao.update(
                    existing.copy(
                        transcript = transcriptOutcome.text,
                        transcriptNote = transcriptOutcome.note,
                        audioPath = metadata.audioFile?.absolutePath ?: existing.audioPath
                    )
                )
            }

            val summarizationEngine = dispatcher.resolveSummarizationEngine(currentPrefs)
            val summaryResult = summarizationEngine.summarize(metadata.caption, transcript)

            val summary = summaryResult.getOrElse { originalErr ->
                return handleError(reelId, "Processing failed: ${originalErr.message}")
            }

            // Step 4: Persist finalized entity in Room DB
            if (existing != null) {
                val updated = existing.copy(
                    creatorHandle = cleanHandle ?: existing.creatorHandle,
                    caption = cleanCaption ?: existing.caption,
                    audioPath = metadata.audioFile?.absolutePath,
                    thumbnailUrl = metadata.thumbnailUrl ?: existing.thumbnailUrl,
                    transcript = transcriptOutcome.text,
                    transcriptNote = transcriptOutcome.note,
                    title = summary.title,
                    oneLineSummary = summary.one_line_summary,
                    takeawaysJson = Json.encodeToString(summary.actionable_takeaways),
                    tagsJson = Json.encodeToString(summary.tags),
                    engineUsed = "${transcriptionEngine.name} + ${summarizationEngine.name}",
                    status = IngestionStatus.COMPLETED,
                    errorMessage = null
                )
                dao.update(updated)
            }

            postCompletionNotification(summary.title, summary.one_line_summary)

            return Result.success()
        } catch (e: Exception) {
            return handleError(reelId, e.message ?: "Unknown ingestion error")
        }
    }

    private suspend fun transcribeExtracted(
        metadata: InstagramExtractor.ExtractedMetadata,
        engine: com.reelrecall.app.engine.TranscriptionEngine
    ): TranscriptOutcome {
        val audio = metadata.audioFile
        val bytes = if (audio != null && audio.exists()) audio.length() else null
        val cloudUpload = engine is GroqWhisperEngine || engine is OpenAIWhisperEngine
        if (bytes == null || bytes <= 0L || (cloudUpload && bytes > TranscriptOutcome.MAX_UPLOAD_BYTES)) {
            return Transcripts.resolve(
                mediaNote = metadata.audioNote,
                audioBytes = bytes,
                transcription = null,
                cloudUpload = cloudUpload
            )
        }
        return Transcripts.resolve(
            mediaNote = metadata.audioNote,
            audioBytes = bytes,
            transcription = engine.transcribe(audio!!),
            cloudUpload = cloudUpload
        )
    }

    private suspend fun handleError(reelId: Long, error: String): Result {
        db.reelDao().updateStatus(reelId, IngestionStatus.FAILED, error)
        return Result.failure()
    }

    private fun postCompletionNotification(title: String, summary: String) {
        val channelId = "reel_library_ingestion"
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
            .setContentTitle("Reel Library: $title")
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
