package com.reelrecall.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptsTest {

    @Test
    fun successStoresTextAndClearsNote() {
        val outcome = Transcripts.resolve(
            mediaNote = null,
            audioBytes = 2_048L,
            transcription = Result.success("  Haan dosto  "),
            cloudUpload = true
        )
        assertEquals("Haan dosto", outcome.text)
        assertNull(outcome.note)
        assertEquals("Haan dosto", outcome.summaryInput)
    }

    @Test
    fun blankSpeechAndSentinelAreNotStored() {
        val blank = Transcripts.resolve(
            mediaNote = null,
            audioBytes = 2_048L,
            transcription = Result.success("   "),
            cloudUpload = true
        )
        assertNull(blank.text)
        assertEquals(TranscriptOutcome.NO_SPEECH, blank.note)

        val sentinel = Transcripts.resolve(
            mediaNote = null,
            audioBytes = 2_048L,
            transcription = Result.success("Not available"),
            cloudUpload = false
        )
        assertNull(sentinel.text)
        assertTrue(sentinel.text != "Not available")
    }

    @Test
    fun missingFileUsesMediaNote() {
        val outcome = Transcripts.resolve(
            mediaNote = "The media asset contains no extractable audio tracks.",
            audioBytes = null,
            transcription = null,
            cloudUpload = true
        )
        assertNull(outcome.text)
        assertEquals("The media asset contains no extractable audio tracks.", outcome.note)
    }

    @Test
    fun engineFailureKeepsTheReason() {
        val outcome = Transcripts.resolve(
            mediaNote = null,
            audioBytes = 4_096L,
            transcription = Result.failure(IllegalStateException("Groq API key is not configured in Settings.")),
            cloudUpload = true
        )
        assertNull(outcome.text)
        assertEquals("Groq API key is not configured in Settings.", outcome.note)
    }

    @Test
    fun oversizedCloudAudioSkipsUpload() {
        val outcome = Transcripts.resolve(
            mediaNote = null,
            audioBytes = TranscriptOutcome.MAX_UPLOAD_BYTES + 1,
            transcription = Result.success("should not be stored"),
            cloudUpload = true
        )
        assertNull(outcome.text)
        assertTrue(outcome.note!!.contains("25 MB"))
    }

    @Test
    fun restrictedMediaUsesRestrictedNote() {
        val outcome = Transcripts.resolve(
            mediaNote = TranscriptOutcome.RESTRICTED_MEDIA,
            audioBytes = null,
            transcription = null,
            cloudUpload = true
        )
        assertNull(outcome.text)
        assertEquals(TranscriptOutcome.RESTRICTED_MEDIA, outcome.note)
        assertTrue(outcome.note!!.contains("restricted"))
    }

    @Test
    fun displayReturnsNoteWhenSpokenIsMissing() {
        val note = "Instagram restricted this Reel (age or login requirement)."
        assertEquals(note, Transcripts.display(null, note))
        assertEquals(note, Transcripts.display("Not available", note))
        assertEquals("Actual spoken text", Transcripts.display("Actual spoken text", note))
    }

    @Test
    fun displayReturnsDefaultWhenBothAreEmpty() {
        val result = Transcripts.display(null, null)
        assertTrue(result.contains("Spoken audio was not transcribed"))
        val resultFromSentinel = Transcripts.display("Not available", "Not available")
        assertTrue(resultFromSentinel.contains("Spoken audio was not transcribed"))
    }
}
