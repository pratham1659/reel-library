package com.reelrecall.app.engine

import android.content.Context
import android.content.SharedPreferences
import com.reelrecall.app.data.repository.AIProvider
import com.reelrecall.app.data.repository.AppPreferences
import com.reelrecall.app.data.repository.EngineMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DualEngineDispatcherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private val fakeStores = mutableMapOf<String, InMemoryTestPreferences>()

    @Before
    fun setup() {
        fakeStores.clear()
        mockContext = object : android.content.ContextWrapper(null) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = tempFolder.root
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
                return fakeStores.getOrPut(name ?: "default") { InMemoryTestPreferences() }
            }
        }
    }

    @Test
    fun testGroqProvider_resolvesGroqEngines() {
        val dispatcher = DualEngineDispatcher(mockContext)
        val prefs = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.GROQ,
            groqApiKey = "gsk_test123"
        )

        val transcription = dispatcher.resolveTranscriptionEngine(prefs)
        val summarizer = dispatcher.resolveSummarizationEngine(prefs)

        assertTrue("Expected GroqWhisperEngine, got ${transcription::class.simpleName}", transcription is GroqWhisperEngine)
        assertEquals("Groq LPU ~ Whisper", transcription.name)

        assertTrue("Expected GroqSummarizerEngine, got ${summarizer::class.simpleName}", summarizer is GroqSummarizerEngine)
        assertEquals("Groq LPU (High Speed Cloud)", summarizer.name)
    }

    @Test
    fun testOpenAIProvider_resolvesOpenAIEngines() {
        val dispatcher = DualEngineDispatcher(mockContext)
        val prefs = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.OPENAI,
            openAIApiKey = "sk-test-openai"
        )

        val transcription = dispatcher.resolveTranscriptionEngine(prefs)
        val summarizer = dispatcher.resolveSummarizationEngine(prefs)

        assertTrue("Expected OpenAIWhisperEngine, got ${transcription::class.simpleName}", transcription is OpenAIWhisperEngine)
        assertEquals("OpenAI Whisper", transcription.name)

        assertTrue("Expected OpenAISummarizerEngine, got ${summarizer::class.simpleName}", summarizer is OpenAISummarizerEngine)
        assertEquals("OpenAI", summarizer.name)
    }

    @Test
    fun testClaudeProvider_resolvesClaudeSummarizerAndHybridTranscription() {
        val dispatcher = DualEngineDispatcher(mockContext)
        // Scenario 1: Claude key + OpenAI key present -> Claude summarizer, OpenAI whisper
        val prefsWithOpenAI = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.CLAUDE,
            claudeApiKey = "sk-ant-test-claude",
            openAIApiKey = "sk-test-openai"
        )

        val summarizer = dispatcher.resolveSummarizationEngine(prefsWithOpenAI)
        val transcription = dispatcher.resolveTranscriptionEngine(prefsWithOpenAI)

        assertTrue("Expected AnthropicClaudeSummarizerEngine, got ${summarizer::class.simpleName}", summarizer is AnthropicClaudeSummarizerEngine)
        assertEquals("Anthropic Claude (3.5)", summarizer.name)

        assertTrue("Expected OpenAIWhisperEngine for Claude hybrid audio, got ${transcription::class.simpleName}", transcription is OpenAIWhisperEngine)
        assertEquals("OpenAI Whisper (Claude Hybrid)", transcription.name)

        // Scenario 2: Claude key + Groq key (no OpenAI key) -> Claude summarizer, Groq whisper
        val prefsWithGroq = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.CLAUDE,
            claudeApiKey = "sk-ant-test-claude",
            groqApiKey = "gsk_test_groq"
        )
        val transcriptionGroq = dispatcher.resolveTranscriptionEngine(prefsWithGroq)
        assertTrue("Expected GroqWhisperEngine fallback, got ${transcriptionGroq::class.simpleName}", transcriptionGroq is GroqWhisperEngine)
    }

    @Test
    fun testCustomProvider_resolvesCustomEndpointAndModels() {
        val dispatcher = DualEngineDispatcher(mockContext)
        val prefs = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.CUSTOM,
            customApiKey = "custom-secret-key",
            customBaseUrl = "https://custom.ai/v1",
            customSummarizerModel = "custom-llm-model",
            customTranscriptionModel = "custom-audio-model"
        )

        val transcription = dispatcher.resolveTranscriptionEngine(prefs)
        val summarizer = dispatcher.resolveSummarizationEngine(prefs)

        assertTrue("Expected OpenAIWhisperEngine with custom config, got ${transcription::class.simpleName}", transcription is OpenAIWhisperEngine)
        assertEquals("Custom Whisper", transcription.name)

        assertTrue("Expected OpenAISummarizerEngine with custom config, got ${summarizer::class.simpleName}", summarizer is OpenAISummarizerEngine)
        assertEquals("Custom (custom-llm-model)", summarizer.name)
    }

    @Test
    fun testMissingKeysInCloudOnlyMode_returnsFailingEngines() {
        val dispatcher = DualEngineDispatcher(mockContext)
        val prefs = AppPreferences(
            engineMode = EngineMode.CLOUD_ONLY,
            selectedProvider = AIProvider.OPENAI,
            openAIApiKey = "" // Missing key
        )

        val transcription = dispatcher.resolveTranscriptionEngine(prefs)
        val summarizer = dispatcher.resolveSummarizationEngine(prefs)

        assertTrue("Expected FailingTranscriptionEngine, got ${transcription::class.simpleName}", transcription is FailingTranscriptionEngine)
        assertTrue("Expected FailingSummarizerEngine, got ${summarizer::class.simpleName}", summarizer is FailingSummarizerEngine)
    }
}

class InMemoryTestPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()
    override fun getAll(): MutableMap<String, *> = HashMap(map)
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (map[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        override fun putString(k: String?, v: String?) = this
        override fun putStringSet(k: String?, v: MutableSet<String>?) = this
        override fun putInt(k: String?, v: Int) = this
        override fun putLong(k: String?, v: Long) = this
        override fun putFloat(k: String?, v: Float) = this
        override fun putBoolean(k: String?, v: Boolean) = this
        override fun remove(k: String?) = this
        override fun clear() = this
        override fun commit() = true
        override fun apply() {}
    }
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
}
