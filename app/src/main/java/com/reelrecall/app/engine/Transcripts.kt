package com.reelrecall.app.engine

/**
 * What ingest stores for one reel's spoken audio.
 * [text] is null unless speech was actually returned. [note] is the user-facing reason.
 * The sentinel "Not available" is never stored.
 */
data class TranscriptOutcome(
    val text: String?,
    val note: String?
) {
    val summaryInput: String
        get() = text?.takeIf { it.isNotBlank() }.orEmpty()

    companion object {
        const val NO_MEDIA =
            "Instagram did not return a downloadable video or audio stream for this reel."
        const val RESTRICTED_MEDIA =
            "Instagram restricted this reel (age-gated or requires account login). Audio stream cannot be downloaded without an authenticated Instagram session."
        const val DOWNLOAD_FAILED =
            "Failed downloading video stream from Instagram, so audio could not be extracted for transcription."
        const val NO_SPEECH =
            "The reel audio was analyzed, but no recognizable spoken speech was detected (contains only music or ambient sounds)."
        const val LOCAL_UNIMPLEMENTED =
            "On-device Whisper is not implemented in this build. Switch to Cloud mode or add a Groq/OpenAI key in Settings."
        const val MIGRATED =
            "Spoken audio was not transcribed on a previous import. Tap 'Retry transcription' to transcribe."
        const val CAPTION_ONLY =
            "Spoken audio is not available. AI Summary and takeaways were generated directly from the post caption."
        const val DEFAULT_UNAVAILABLE =
            "Spoken audio was not transcribed for this reel. The audio may contain only background music or the media stream was restricted by Instagram."
        const val MAX_UPLOAD_BYTES = 25L * 1024L * 1024L

        fun ready(text: String) = TranscriptOutcome(text = text, note = null)

        fun unavailable(note: String) = TranscriptOutcome(text = null, note = note)
    }
}

object Transcripts {
    /**
     * @param audioBytes null when no audio file exists. A positive size is a real file.
     * @param transcription null when the engine was not called.
     */
    fun resolve(
        mediaNote: String?,
        audioBytes: Long?,
        transcription: Result<String>?,
        cloudUpload: Boolean
    ): TranscriptOutcome {
        if (audioBytes == null || audioBytes <= 0L) {
            return TranscriptOutcome.unavailable(mediaNote?.takeIf { it.isNotBlank() } ?: TranscriptOutcome.NO_MEDIA)
        }
        if (cloudUpload && audioBytes > TranscriptOutcome.MAX_UPLOAD_BYTES) {
            val megabytes = audioBytes / (1024L * 1024L)
            return TranscriptOutcome.unavailable(
                "Audio file exceeds the 25 MB upload limit. Size: $megabytes MB"
            )
        }
        val result = transcription
            ?: return TranscriptOutcome.unavailable(mediaNote?.takeIf { it.isNotBlank() } ?: TranscriptOutcome.NO_MEDIA)
        result.exceptionOrNull()?.let { error ->
            val message = error.message?.trim().orEmpty()
            return TranscriptOutcome.unavailable(message.ifBlank { "Transcription failed." })
        }
        val trimmed = result.getOrNull()?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.equals("Not available", ignoreCase = true)) {
            return TranscriptOutcome.unavailable(TranscriptOutcome.NO_SPEECH)
        }
        return TranscriptOutcome.ready(trimmed)
    }

    fun display(transcript: String?, note: String? = null): String {
        val spoken = transcript?.trim().orEmpty()
        if (spoken.isNotEmpty() && !spoken.equals("Not available", ignoreCase = true)) {
            return spoken
        }
        val cleanNote = note?.trim().orEmpty()
        if (cleanNote.isNotEmpty() && !cleanNote.equals("Not available", ignoreCase = true)) {
            return cleanNote
        }
        return TranscriptOutcome.DEFAULT_UNAVAILABLE
    }

    fun reasonForUnavailable(transcript: String?, note: String? = null): String {
        if (hasSpokenTranscript(transcript)) return ""
        val cleanNote = note?.trim().orEmpty()
        if (cleanNote.isNotEmpty() && !cleanNote.equals("Not available", ignoreCase = true)) {
            return cleanNote
        }
        return TranscriptOutcome.DEFAULT_UNAVAILABLE
    }

    fun hasSpokenTranscript(transcript: String?): Boolean {
        val spoken = transcript?.trim().orEmpty()
        return spoken.isNotEmpty() && !spoken.equals("Not available", ignoreCase = true)
    }
}
