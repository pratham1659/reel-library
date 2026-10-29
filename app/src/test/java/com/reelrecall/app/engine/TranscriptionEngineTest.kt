package com.reelrecall.app.engine

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

class TranscriptionEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testEmptyFile_failsBeforeNetwork() = runBlocking {
        val emptyFile = tempFolder.newFile("empty_audio.mp4")
        val networkHit = AtomicBoolean(false)

        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            chain.proceed(chain.request())
        }.build()

        val engine = GroqWhisperEngine(apiKey = "gsk_test123", client = client)
        val result = engine.transcribe(emptyFile)

        assertTrue(result.isFailure)
        assertFalse("Network must not be reached for empty file", networkHit.get())
        assertTrue(result.exceptionOrNull()?.message?.contains("No valid audio stream available") == true)
    }

    @Test
    fun testNonExistentFile_failsBeforeNetwork() = runBlocking {
        val nonExistent = File(tempFolder.root, "does_not_exist.mp3")
        val networkHit = AtomicBoolean(false)

        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            chain.proceed(chain.request())
        }.build()

        val engine = GroqWhisperEngine(apiKey = "gsk_test123", client = client)
        val result = engine.transcribe(nonExistent)

        assertTrue(result.isFailure)
        assertFalse("Network must not be reached for non-existent file", networkHit.get())
    }

    @Test
    fun testOverLimitFile_failsBeforeNetwork() = runBlocking {
        val overLimitFile = tempFolder.newFile("over_limit.mp3")
        // 25 MB + 1 byte
        val raf = RandomAccessFile(overLimitFile, "rw")
        raf.setLength(GroqWhisperEngine.MAX_GROQ_AUDIO_BYTES + 1L)
        raf.close()

        val networkHit = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            chain.proceed(chain.request())
        }.build()

        val engine = GroqWhisperEngine(apiKey = "gsk_test123", client = client)
        val result = engine.transcribe(overLimitFile)

        assertTrue(result.isFailure)
        assertFalse("Network must not be reached for file > 25 MB", networkHit.get())
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(msg.contains("exceeds Groq maximum upload limit (25 MB)"))
    }

    @Test
    fun testBelowLimitFile_passesGuardToNetwork() = runBlocking {
        val smallFile = tempFolder.newFile("valid_small.mp3")
        smallFile.writeBytes(ByteArray(1024) { 1 }) // 1 KB

        val networkHit = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"text": "Sample transcribed audio"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()

        val engine = GroqWhisperEngine(apiKey = "gsk_test123", client = client)
        val result = engine.transcribe(smallFile)

        assertTrue(result.isSuccess)
        assertTrue("Network must be reached for valid small audio file", networkHit.get())
        assertEquals("Sample transcribed audio", result.getOrNull())
    }

    @Test
    fun testAtLimitFile_passesGuardToNetwork() = runBlocking {
        val atLimitFile = tempFolder.newFile("at_limit.mp3")
        val raf = RandomAccessFile(atLimitFile, "rw")
        raf.setLength(GroqWhisperEngine.MAX_GROQ_AUDIO_BYTES) // Exactly 25 MB (26,214,400 bytes)
        raf.close()

        val networkHit = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"text": "Transcribed at 25MB limit"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()

        val engine = GroqWhisperEngine(apiKey = "gsk_test123", client = client)
        val result = engine.transcribe(atLimitFile)

        assertTrue(result.isSuccess)
        assertTrue("Network must be reached for file exactly at 25 MB", networkHit.get())
        assertEquals("Transcribed at 25MB limit", result.getOrNull())
    }

    @Test
    fun testBlankApiKey_failsBeforeNetwork() = runBlocking {
        val audioFile = tempFolder.newFile("audio.mp3")
        audioFile.writeBytes(ByteArray(512))

        val networkHit = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            networkHit.set(true)
            chain.proceed(chain.request())
        }.build()

        val engine = GroqWhisperEngine(apiKey = "   ", client = client)
        val result = engine.transcribe(audioFile)

        assertTrue(result.isFailure)
        assertFalse("Network must not be reached when API key is blank", networkHit.get())
        assertTrue(result.exceptionOrNull()?.message?.contains("Groq API key is not configured") == true)
    }
}
