package com.reelrecall.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "reel_library_preferences")

enum class EngineMode(val displayName: String, val description: String) {
    CLOUD_ONLY(
        displayName = "Cloud Free-Tier (Groq LPU)",
        description = "Sub-second processing via free API keys. Minimal battery and storage usage."
    ),
    LOCAL_ONLY(
        displayName = "100% On-Device (Whisper + Gemma)",
        description = "Fully offline and private. Leverages your phone's 12GB RAM and NPU/GPU."
    ),
    AUTO_HYBRID(
        displayName = "Auto / Hybrid Fallback",
        description = "Uses Cloud for speed when online, seamlessly switches to On-Device when offline."
    )
}

data class AppPreferences(
    val engineMode: EngineMode = EngineMode.CLOUD_ONLY,
    val groqApiKey: String = "",
    val isWhisperModelDownloaded: Boolean = false,
    val isLlmModelDownloaded: Boolean = false,
    val whisperModelPath: String? = null,
    val llmModelPath: String? = null,
    val showTagsOnCards: Boolean = true,
    val cardClickAction: String = "READER", // "READER", "TRANSCRIPT", "ORIGINAL"
    val autoSummarization: Boolean = true,
    val summaryStyle: String = "CONCISE", // "CONCISE", "DETAILED"
    val autoTagging: Boolean = true,
    val themeMode: String = "OBSIDIAN_DARK", // "OBSIDIAN_DARK", "OBSIDIAN_LIGHT", "SYSTEM"
    val isLoggedIn: Boolean = false,
    val userName: String = "Not logged in",
    val userEmail: String = "",
    val userAvatarInitial: String = "?",
    val isCloudBackupEnabled: Boolean = false,
    val backupOnlyOnWifi: Boolean = true,
    val cloudBackupFolder: String = "ReelLibrary_Vault",
    val lastBackupTimeFormatted: String = "Never backed up",
    // Real-Time Cloud Telemetry & Token Tracking (Zero demo data)
    val totalTokensUsed: Long = 0L,
    val totalPromptTokens: Long = 0L,
    val totalCompletionTokens: Long = 0L,
    val lastCallTokens: Long = 0L,
    val lastCallPromptTokens: Long = 0L,
    val lastCallCompletionTokens: Long = 0L,
    val lastCallLatencyMs: Long = 0L,
    val lastCallStatusCode: String = "",
    val lastCallProvider: String = "",
    // Per-Model Last Call Telemetry
    val lastCallQwenTokens: Long = 0L,
    val lastCallQwenPromptTokens: Long = 0L,
    val lastCallQwenCompletionTokens: Long = 0L,
    val lastCallQwenLatencyMs: Long = 0L,
    val lastCallQwenStatusCode: String = "",
    val lastCallQwenProvider: String = "",
    val lastCallQwenModel: String = "",
    val lastCallWhisperTokens: Long = 0L,
    val lastCallWhisperPromptTokens: Long = 0L,
    val lastCallWhisperCompletionTokens: Long = 0L,
    val lastCallWhisperLatencyMs: Long = 0L,
    val lastCallWhisperStatusCode: String = "",
    val lastCallWhisperProvider: String = "",
    val lastCallWhisperModel: String = "",
    // Per-API Token Tracking
    val groqTotalTokens: Long = 0L,
    val groqPromptTokens: Long = 0L,
    val groqCompletionTokens: Long = 0L,
    val groqCallsCount: Long = 0L,
    val groqWhisperTokens: Long = 0L,
    val groqWhisperCalls: Long = 0L,
    val groqWhisperAudioSeconds: Long = 0L,
    val lastCallWhisperAudioSeconds: Long = 0L,
    val groqWhisperOutputWords: Long = 0L,
    val lastCallWhisperOutputWords: Long = 0L,
    val groqQwenTokens: Long = 0L,
    val groqQwenPromptTokens: Long = 0L,
    val groqQwenCompletionTokens: Long = 0L,
    val groqQwenCalls: Long = 0L,
    // Per-Model Local Telemetry
    val lastCallLocalGemmaTokens: Long = 0L,
    val lastCallLocalGemmaPromptTokens: Long = 0L,
    val lastCallLocalGemmaCompletionTokens: Long = 0L,
    val lastCallLocalGemmaLatencyMs: Long = 0L,
    val lastCallLocalGemmaStatusCode: String = "",
    val lastCallLocalGemmaProvider: String = "",
    val lastCallLocalGemmaModel: String = "",
    val lastCallLocalWhisperTokens: Long = 0L,
    val lastCallLocalWhisperPromptTokens: Long = 0L,
    val lastCallLocalWhisperCompletionTokens: Long = 0L,
    val lastCallLocalWhisperLatencyMs: Long = 0L,
    val lastCallLocalWhisperStatusCode: String = "",
    val lastCallLocalWhisperProvider: String = "",
    val lastCallLocalWhisperModel: String = "",
    val localTotalTokens: Long = 0L,
    val localPromptTokens: Long = 0L,
    val localCompletionTokens: Long = 0L,
    val localGemmaCallsCount: Long = 0L,
    val localWhisperCallsCount: Long = 0L
)

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ENGINE_MODE = stringPreferencesKey("engine_mode")
        val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        val IS_WHISPER_DOWNLOADED = booleanPreferencesKey("is_whisper_downloaded")
        val IS_LLM_DOWNLOADED = booleanPreferencesKey("is_llm_downloaded")
        val WHISPER_PATH = stringPreferencesKey("whisper_path")
        val LLM_PATH = stringPreferencesKey("llm_path")
        val SHOW_TAGS_ON_CARDS = booleanPreferencesKey("show_tags_on_cards")
        val CARD_CLICK_ACTION = stringPreferencesKey("card_click_action")
        val AUTO_SUMMARIZATION = booleanPreferencesKey("auto_summarization")
        val SUMMARY_STYLE = stringPreferencesKey("summary_style")
        val AUTO_TAGGING = booleanPreferencesKey("auto_tagging")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_AVATAR_INITIAL = stringPreferencesKey("user_avatar_initial")
        val IS_CLOUD_BACKUP_ENABLED = booleanPreferencesKey("is_cloud_backup_enabled")
        val BACKUP_ONLY_ON_WIFI = booleanPreferencesKey("backup_only_on_wifi")
        val CLOUD_BACKUP_FOLDER = stringPreferencesKey("cloud_backup_folder")
        val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
        // Real-Time Cloud Telemetry Keys
        val TOTAL_TOKENS_USED = longPreferencesKey("total_tokens_used")
        val TOTAL_PROMPT_TOKENS = longPreferencesKey("total_prompt_tokens")
        val TOTAL_COMPLETION_TOKENS = longPreferencesKey("total_completion_tokens")
        val LAST_CALL_TOKENS = longPreferencesKey("last_call_tokens")
        val LAST_CALL_PROMPT_TOKENS = longPreferencesKey("last_call_prompt_tokens")
        val LAST_CALL_COMPLETION_TOKENS = longPreferencesKey("last_call_completion_tokens")
        val LAST_CALL_LATENCY_MS = longPreferencesKey("last_call_latency_ms")
        val LAST_CALL_STATUS_CODE = stringPreferencesKey("last_call_status_code")
        val LAST_CALL_PROVIDER = stringPreferencesKey("last_call_provider")
        val LAST_DAILY_RESET_TIME = longPreferencesKey("last_daily_reset_time")
        // Per-Model Last Call Telemetry Keys
        val LAST_CALL_QWEN_TOKENS = longPreferencesKey("last_call_qwen_tokens")
        val LAST_CALL_QWEN_PROMPT_TOKENS = longPreferencesKey("last_call_qwen_prompt_tokens")
        val LAST_CALL_QWEN_COMPLETION_TOKENS = longPreferencesKey("last_call_qwen_completion_tokens")
        val LAST_CALL_QWEN_LATENCY_MS = longPreferencesKey("last_call_qwen_latency_ms")
        val LAST_CALL_QWEN_STATUS_CODE = stringPreferencesKey("last_call_qwen_status_code")
        val LAST_CALL_QWEN_PROVIDER = stringPreferencesKey("last_call_qwen_provider")
        val LAST_CALL_QWEN_MODEL = stringPreferencesKey("last_call_qwen_model")

        val LAST_CALL_WHISPER_TOKENS = longPreferencesKey("last_call_whisper_tokens")
        val LAST_CALL_WHISPER_PROMPT_TOKENS = longPreferencesKey("last_call_whisper_prompt_tokens")
        val LAST_CALL_WHISPER_COMPLETION_TOKENS = longPreferencesKey("last_call_whisper_completion_tokens")
        val LAST_CALL_WHISPER_LATENCY_MS = longPreferencesKey("last_call_whisper_latency_ms")
        val LAST_CALL_WHISPER_STATUS_CODE = stringPreferencesKey("last_call_whisper_status_code")
        val LAST_CALL_WHISPER_PROVIDER = stringPreferencesKey("last_call_whisper_provider")
        val LAST_CALL_WHISPER_MODEL = stringPreferencesKey("last_call_whisper_model")
        // Per-Provider Telemetry Keys
        val GROQ_TOTAL_TOKENS = longPreferencesKey("groq_total_tokens")
        val GROQ_PROMPT_TOKENS = longPreferencesKey("groq_prompt_tokens")
        val GROQ_COMPLETION_TOKENS = longPreferencesKey("groq_completion_tokens")
        val GROQ_CALLS_COUNT = longPreferencesKey("groq_calls_count")
        val GROQ_WHISPER_TOTAL_TOKENS = longPreferencesKey("groq_whisper_total_tokens")
        val GROQ_WHISPER_CALLS_COUNT = longPreferencesKey("groq_whisper_calls_count")
        val GROQ_WHISPER_AUDIO_SECONDS = longPreferencesKey("groq_whisper_audio_seconds")
        val LAST_CALL_WHISPER_AUDIO_SECONDS = longPreferencesKey("last_call_whisper_audio_seconds")
        val GROQ_WHISPER_OUTPUT_WORDS = longPreferencesKey("groq_whisper_output_words")
        val LAST_CALL_WHISPER_OUTPUT_WORDS = longPreferencesKey("last_call_whisper_output_words")
        val GROQ_QWEN_TOTAL_TOKENS = longPreferencesKey("groq_qwen_total_tokens")
        val GROQ_QWEN_PROMPT_TOKENS = longPreferencesKey("groq_qwen_prompt_tokens")
        val GROQ_QWEN_COMPLETION_TOKENS = longPreferencesKey("groq_qwen_completion_tokens")
        val GROQ_QWEN_CALLS_COUNT = longPreferencesKey("groq_qwen_calls_count")

        // Local On-Device Telemetry Keys
        val LAST_CALL_LOCAL_GEMMA_TOKENS = longPreferencesKey("last_call_local_gemma_tokens")
        val LAST_CALL_LOCAL_GEMMA_PROMPT_TOKENS = longPreferencesKey("last_call_local_gemma_prompt_tokens")
        val LAST_CALL_LOCAL_GEMMA_COMPLETION_TOKENS = longPreferencesKey("last_call_local_gemma_completion_tokens")
        val LAST_CALL_LOCAL_GEMMA_LATENCY_MS = longPreferencesKey("last_call_local_gemma_latency_ms")
        val LAST_CALL_LOCAL_GEMMA_STATUS_CODE = stringPreferencesKey("last_call_local_gemma_status_code")
        val LAST_CALL_LOCAL_GEMMA_PROVIDER = stringPreferencesKey("last_call_local_gemma_provider")
        val LAST_CALL_LOCAL_GEMMA_MODEL = stringPreferencesKey("last_call_local_gemma_model")

        val LAST_CALL_LOCAL_WHISPER_TOKENS = longPreferencesKey("last_call_local_whisper_tokens")
        val LAST_CALL_LOCAL_WHISPER_PROMPT_TOKENS = longPreferencesKey("last_call_local_whisper_prompt_tokens")
        val LAST_CALL_LOCAL_WHISPER_COMPLETION_TOKENS = longPreferencesKey("last_call_local_whisper_completion_tokens")
        val LAST_CALL_LOCAL_WHISPER_LATENCY_MS = longPreferencesKey("last_call_local_whisper_latency_ms")
        val LAST_CALL_LOCAL_WHISPER_STATUS_CODE = stringPreferencesKey("last_call_local_whisper_status_code")
        val LAST_CALL_LOCAL_WHISPER_PROVIDER = stringPreferencesKey("last_call_local_whisper_provider")
        val LAST_CALL_LOCAL_WHISPER_MODEL = stringPreferencesKey("last_call_local_whisper_model")

        val LOCAL_TOTAL_TOKENS = longPreferencesKey("local_total_tokens")
        val LOCAL_PROMPT_TOKENS = longPreferencesKey("local_prompt_tokens")
        val LOCAL_COMPLETION_TOKENS = longPreferencesKey("local_completion_tokens")
        val LOCAL_GEMMA_CALLS_COUNT = longPreferencesKey("local_gemma_calls_count")
        val LOCAL_WHISPER_CALLS_COUNT = longPreferencesKey("local_whisper_calls_count")
    }

    init {
        try {
            val oldPrefFile = java.io.File(context.filesDir, "datastore/reel_recall_preferences.preferences_pb")
            val newPrefFile = java.io.File(context.filesDir, "datastore/reel_library_preferences.preferences_pb")
            if (oldPrefFile.exists() && !newPrefFile.exists()) {
                newPrefFile.parentFile?.mkdirs()
                oldPrefFile.copyTo(newPrefFile, overwrite = true)
                android.util.Log.i("PreferencesRepo", "Migrated reel_recall_preferences to reel_library_preferences")
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesRepo", "Error migrating preferences file", e)
        }
    }

    private val db by lazy { com.reelrecall.app.data.local.AppDatabase.getInstance(context) }
    private val tokenUsageDao by lazy { db.tokenUsageDao() }

    private val secureKeyVault = com.reelrecall.app.data.security.SecureKeyVault.getInstance(context)

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        val modeString = preferences[PreferencesKeys.ENGINE_MODE] ?: EngineMode.CLOUD_ONLY.name
        val mode = try {
            EngineMode.valueOf(modeString)
        } catch (e: Exception) {
            EngineMode.CLOUD_ONLY
        }

        // Retrieve securely from hardware-backed Keystore vault
        val groqKey = secureKeyVault.getGroqApiKey()

        val lastProvider = preferences[PreferencesKeys.LAST_CALL_PROVIDER] ?: ""
        val lastLatency = preferences[PreferencesKeys.LAST_CALL_LATENCY_MS] ?: 0L
        val lastStatus = preferences[PreferencesKeys.LAST_CALL_STATUS_CODE] ?: ""
        val lastTokens = preferences[PreferencesKeys.LAST_CALL_TOKENS] ?: 0L
        val lastPrompt = preferences[PreferencesKeys.LAST_CALL_PROMPT_TOKENS] ?: 0L
        val lastComp = preferences[PreferencesKeys.LAST_CALL_COMPLETION_TOKENS] ?: 0L

        val qwenTokens = preferences[PreferencesKeys.LAST_CALL_QWEN_TOKENS] ?: lastTokens
        val qwenPrompt = preferences[PreferencesKeys.LAST_CALL_QWEN_PROMPT_TOKENS] ?: lastPrompt
        val qwenComp = preferences[PreferencesKeys.LAST_CALL_QWEN_COMPLETION_TOKENS] ?: lastComp
        val qwenLatency = preferences[PreferencesKeys.LAST_CALL_QWEN_LATENCY_MS] ?: (if (!lastProvider.contains("Whisper", ignoreCase = true)) lastLatency else 0L)
        val qwenStatus = preferences[PreferencesKeys.LAST_CALL_QWEN_STATUS_CODE] ?: (if (!lastProvider.contains("Whisper", ignoreCase = true)) lastStatus else "200 OK")
        val qwenProvider = preferences[PreferencesKeys.LAST_CALL_QWEN_PROVIDER] ?: "Groq LPU ~ Qwen"
        val qwenModel = preferences[PreferencesKeys.LAST_CALL_QWEN_MODEL] ?: "qwen/qwen3.8-27b"

        val whisperTokens = preferences[PreferencesKeys.LAST_CALL_WHISPER_TOKENS] ?: 0L
        val whisperPrompt = preferences[PreferencesKeys.LAST_CALL_WHISPER_PROMPT_TOKENS] ?: 0L
        val whisperComp = preferences[PreferencesKeys.LAST_CALL_WHISPER_COMPLETION_TOKENS] ?: 0L
        val whisperLatency = preferences[PreferencesKeys.LAST_CALL_WHISPER_LATENCY_MS] ?: (if (lastProvider.contains("Whisper", ignoreCase = true)) lastLatency else 0L)
        val whisperStatus = preferences[PreferencesKeys.LAST_CALL_WHISPER_STATUS_CODE] ?: (if (lastProvider.contains("Whisper", ignoreCase = true)) lastStatus else "200 OK")
        val whisperProvider = preferences[PreferencesKeys.LAST_CALL_WHISPER_PROVIDER] ?: "Groq LPU ~ Whisper"
        val whisperModel = preferences[PreferencesKeys.LAST_CALL_WHISPER_MODEL] ?: "whisper-large-v3-turbo"
        val customWhisperPath = preferences[PreferencesKeys.WHISPER_PATH]
        val whisperFile = if (!customWhisperPath.isNullOrBlank() && java.io.File(customWhisperPath).exists()) {
            java.io.File(customWhisperPath)
        } else {
            java.io.File(context.filesDir, "whisper-base.bin")
        }
        val customLlmPath = preferences[PreferencesKeys.LLM_PATH]
        val llmFile = if (!customLlmPath.isNullOrBlank() && java.io.File(customLlmPath).exists()) {
            java.io.File(customLlmPath)
        } else {
            java.io.File(context.filesDir, "gemma-2-2b-it-gpu.bin")
        }
        val isWhisperReal = (preferences[PreferencesKeys.IS_WHISPER_DOWNLOADED] ?: false) && whisperFile.exists() && whisperFile.length() > 10_000_000L
        val isLlmReal = (preferences[PreferencesKeys.IS_LLM_DOWNLOADED] ?: false) && llmFile.exists() && llmFile.length() > 50_000_000L

        AppPreferences(
            engineMode = mode,
            groqApiKey = groqKey,
            isWhisperModelDownloaded = isWhisperReal,
            isLlmModelDownloaded = isLlmReal,
            whisperModelPath = preferences[PreferencesKeys.WHISPER_PATH],
            llmModelPath = preferences[PreferencesKeys.LLM_PATH],
            showTagsOnCards = preferences[PreferencesKeys.SHOW_TAGS_ON_CARDS] ?: true,
            cardClickAction = preferences[PreferencesKeys.CARD_CLICK_ACTION] ?: "READER",
            autoSummarization = preferences[PreferencesKeys.AUTO_SUMMARIZATION] ?: true,
            summaryStyle = preferences[PreferencesKeys.SUMMARY_STYLE] ?: "CONCISE",
            autoTagging = preferences[PreferencesKeys.AUTO_TAGGING] ?: true,
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "OBSIDIAN_DARK",
            isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "Not logged in",
            userEmail = preferences[PreferencesKeys.USER_EMAIL] ?: "",
            userAvatarInitial = preferences[PreferencesKeys.USER_AVATAR_INITIAL] ?: "?",
            isCloudBackupEnabled = preferences[PreferencesKeys.IS_CLOUD_BACKUP_ENABLED] ?: false,
            backupOnlyOnWifi = preferences[PreferencesKeys.BACKUP_ONLY_ON_WIFI] ?: true,
            cloudBackupFolder = preferences[PreferencesKeys.CLOUD_BACKUP_FOLDER] ?: "ReelLibrary_Vault",
            lastBackupTimeFormatted = preferences[PreferencesKeys.LAST_BACKUP_TIME] ?: "Never backed up",
            totalTokensUsed = preferences[PreferencesKeys.TOTAL_TOKENS_USED] ?: 0L,
            totalPromptTokens = preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] ?: 0L,
            totalCompletionTokens = preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] ?: 0L,
            lastCallTokens = lastTokens,
            lastCallPromptTokens = lastPrompt,
            lastCallCompletionTokens = lastComp,
            lastCallLatencyMs = lastLatency,
            lastCallStatusCode = lastStatus,
            lastCallProvider = lastProvider,
            lastCallQwenTokens = qwenTokens,
            lastCallQwenPromptTokens = qwenPrompt,
            lastCallQwenCompletionTokens = qwenComp,
            lastCallQwenLatencyMs = qwenLatency,
            lastCallQwenStatusCode = qwenStatus,
            lastCallQwenProvider = qwenProvider,
            lastCallQwenModel = qwenModel,
            lastCallWhisperTokens = whisperTokens,
            lastCallWhisperPromptTokens = whisperPrompt,
            lastCallWhisperCompletionTokens = whisperComp,
            lastCallWhisperLatencyMs = whisperLatency,
            lastCallWhisperStatusCode = whisperStatus,
            lastCallWhisperProvider = whisperProvider,
            lastCallWhisperModel = whisperModel,
            groqTotalTokens = preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] ?: 0L,
            groqPromptTokens = preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] ?: 0L,
            groqCompletionTokens = preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] ?: 0L,
            groqCallsCount = preferences[PreferencesKeys.GROQ_CALLS_COUNT] ?: 0L,
            groqWhisperTokens = preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] ?: 0L,
            groqWhisperCalls = preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] ?: 0L,
            groqWhisperAudioSeconds = preferences[PreferencesKeys.GROQ_WHISPER_AUDIO_SECONDS] ?: 0L,
            lastCallWhisperAudioSeconds = preferences[PreferencesKeys.LAST_CALL_WHISPER_AUDIO_SECONDS] ?: 0L,
            groqWhisperOutputWords = preferences[PreferencesKeys.GROQ_WHISPER_OUTPUT_WORDS] ?: 0L,
            lastCallWhisperOutputWords = preferences[PreferencesKeys.LAST_CALL_WHISPER_OUTPUT_WORDS] ?: 0L,
            groqQwenTokens = preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] ?: (preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] ?: 0L),
            groqQwenPromptTokens = preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] ?: (preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] ?: 0L),
            groqQwenCompletionTokens = preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] ?: (preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] ?: 0L),
            groqQwenCalls = preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] ?: (preferences[PreferencesKeys.GROQ_CALLS_COUNT] ?: 0L),
            // Local On-Device Telemetry
            lastCallLocalGemmaTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_TOKENS] ?: 0L,
            lastCallLocalGemmaPromptTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_PROMPT_TOKENS] ?: 0L,
            lastCallLocalGemmaCompletionTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_COMPLETION_TOKENS] ?: 0L,
            lastCallLocalGemmaLatencyMs = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_LATENCY_MS] ?: 0L,
            lastCallLocalGemmaStatusCode = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_STATUS_CODE] ?: "200 OK",
            lastCallLocalGemmaProvider = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_PROVIDER] ?: "On-Device Gemma 2B",
            lastCallLocalGemmaModel = preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_MODEL] ?: "gemma-2-2b-it-gpu",
            lastCallLocalWhisperTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_TOKENS] ?: 0L,
            lastCallLocalWhisperPromptTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_PROMPT_TOKENS] ?: 0L,
            lastCallLocalWhisperCompletionTokens = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_COMPLETION_TOKENS] ?: 0L,
            lastCallLocalWhisperLatencyMs = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_LATENCY_MS] ?: 0L,
            lastCallLocalWhisperStatusCode = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_STATUS_CODE] ?: "200 OK",
            lastCallLocalWhisperProvider = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_PROVIDER] ?: "On-Device Whisper Base",
            lastCallLocalWhisperModel = preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_MODEL] ?: "whisper.cpp-base",
            localTotalTokens = preferences[PreferencesKeys.LOCAL_TOTAL_TOKENS] ?: 0L,
            localPromptTokens = preferences[PreferencesKeys.LOCAL_PROMPT_TOKENS] ?: 0L,
            localCompletionTokens = preferences[PreferencesKeys.LOCAL_COMPLETION_TOKENS] ?: 0L,
            localGemmaCallsCount = preferences[PreferencesKeys.LOCAL_GEMMA_CALLS_COUNT] ?: 0L,
            localWhisperCallsCount = preferences[PreferencesKeys.LOCAL_WHISPER_CALLS_COUNT] ?: 0L
        )
    }

    suspend fun setEngineMode(mode: EngineMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENGINE_MODE] = mode.name
        }
    }

    suspend fun setGroqApiKey(key: String) {
        if (key.isBlank()) {
            deleteGroqApiKey()
        } else {
            secureKeyVault.setGroqApiKey(key)
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.GROQ_API_KEY)
            }
        }
    }

    suspend fun deleteGroqApiKey() {
        secureKeyVault.deleteGroqApiKey()
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.GROQ_API_KEY)
        }
    }

    suspend fun setLocalModelStatus(
        isWhisper: Boolean,
        isDownloaded: Boolean,
        path: String? = null
    ) {
        context.dataStore.edit { preferences ->
            if (isWhisper) {
                preferences[PreferencesKeys.IS_WHISPER_DOWNLOADED] = isDownloaded
                if (path != null) preferences[PreferencesKeys.WHISPER_PATH] = path
            } else {
                preferences[PreferencesKeys.IS_LLM_DOWNLOADED] = isDownloaded
                if (path != null) preferences[PreferencesKeys.LLM_PATH] = path
            }
        }
    }

    suspend fun setShowTagsOnCards(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TAGS_ON_CARDS] = show
        }
    }

    suspend fun setCardClickAction(action: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARD_CLICK_ACTION] = action
        }
    }

    suspend fun setAutoSummarization(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SUMMARIZATION] = enabled
        }
    }

    suspend fun setSummaryStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_STYLE] = style
        }
    }

    suspend fun setAutoTagging(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_TAGGING] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLoginState(isLoggedIn: Boolean, name: String, email: String, initial: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_AVATAR_INITIAL] = initial
        }
    }

    suspend fun setCloudBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_CLOUD_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setBackupOnlyOnWifi(onlyWifi: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKUP_ONLY_ON_WIFI] = onlyWifi
        }
    }

    suspend fun updateLastBackupTime(timeStr: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIME] = timeStr
        }
    }

    private fun getMidnightCutoff(now: Long = System.currentTimeMillis()): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    suspend fun checkAndPerformDailyResetIfNeeded() {
        val now = System.currentTimeMillis()
        val cutoff = getMidnightCutoff(now)
        context.dataStore.edit { preferences ->
            val lastReset = preferences[PreferencesKeys.LAST_DAILY_RESET_TIME] ?: 0L
            if (lastReset < cutoff) {
                preferences[PreferencesKeys.TOTAL_TOKENS_USED] = 0L
                preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] = 0L
                preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_CALLS_COUNT] = 0L
                preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] = 0L
                preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] = 0L
                preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] = 0L
                preferences[PreferencesKeys.LOCAL_TOTAL_TOKENS] = 0L
                preferences[PreferencesKeys.LOCAL_PROMPT_TOKENS] = 0L
                preferences[PreferencesKeys.LOCAL_COMPLETION_TOKENS] = 0L
                preferences[PreferencesKeys.LOCAL_GEMMA_CALLS_COUNT] = 0L
                preferences[PreferencesKeys.LOCAL_WHISPER_CALLS_COUNT] = 0L
                preferences[PreferencesKeys.LAST_DAILY_RESET_TIME] = now
            }
        }
    }

    suspend fun recordTokenUsage(
        promptTokens: Long,
        completionTokens: Long,
        latencyMs: Long = 0L,
        statusCode: String = "200 OK",
        provider: String = "",
        model: String = "",
        errorMessage: String? = null,
        audioSeconds: Long = 0L
    ) {
        checkAndPerformDailyResetIfNeeded()

        val now = System.currentTimeMillis()
        val dateFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val monthFmt = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        val dateStr = dateFmt.format(java.util.Date(now))
        val monthStr = monthFmt.format(java.util.Date(now))
        val totalForCall = promptTokens + completionTokens
        val isLocal = (provider.contains("Whisper", ignoreCase = true) && !provider.contains("Groq", ignoreCase = true)) ||
                provider.contains("Gemma", ignoreCase = true) ||
                provider.contains("Local", ignoreCase = true) ||
                provider.contains("On-Device", ignoreCase = true)
        val engineType = if (isLocal) "LOCAL" else "CLOUD"

        // 1. Permanently record full history into Room DB
        try {
            tokenUsageDao.insert(
                com.reelrecall.app.data.model.TokenUsageEntity(
                    timestamp = now,
                    dateString = dateStr,
                    monthString = monthStr,
                    engineType = engineType,
                    provider = provider,
                    promptTokens = promptTokens,
                    completionTokens = completionTokens,
                    totalTokens = totalForCall,
                    latencyMs = latencyMs,
                    statusCode = statusCode,
                    errorDetails = errorMessage,
                    payloadSnippet = model
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("PreferencesRepository", "Failed to insert TokenUsageEntity", e)
        }

        // 2. Update real-time daily DataStore telemetry
        context.dataStore.edit { preferences ->
            val currentPrompt = preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] ?: 0L
            val currentComp = preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] ?: 0L
            val currentTotal = preferences[PreferencesKeys.TOTAL_TOKENS_USED] ?: 0L

            preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] = currentPrompt + promptTokens
            preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] = currentComp + completionTokens
            preferences[PreferencesKeys.TOTAL_TOKENS_USED] = currentTotal + totalForCall
            preferences[PreferencesKeys.LAST_CALL_TOKENS] = totalForCall
            preferences[PreferencesKeys.LAST_CALL_PROMPT_TOKENS] = promptTokens
            preferences[PreferencesKeys.LAST_CALL_COMPLETION_TOKENS] = completionTokens
            if (latencyMs > 0L) {
                preferences[PreferencesKeys.LAST_CALL_LATENCY_MS] = latencyMs
            }
            if (statusCode.isNotBlank()) {
                preferences[PreferencesKeys.LAST_CALL_STATUS_CODE] = statusCode
            }
            if (provider.isNotBlank()) {
                preferences[PreferencesKeys.LAST_CALL_PROVIDER] = provider
            }

            // Local On-Device telemetry tracking
            if (isLocal) {
                val locTotal = preferences[PreferencesKeys.LOCAL_TOTAL_TOKENS] ?: 0L
                val locPrompt = preferences[PreferencesKeys.LOCAL_PROMPT_TOKENS] ?: 0L
                val locComp = preferences[PreferencesKeys.LOCAL_COMPLETION_TOKENS] ?: 0L

                preferences[PreferencesKeys.LOCAL_TOTAL_TOKENS] = locTotal + totalForCall
                preferences[PreferencesKeys.LOCAL_PROMPT_TOKENS] = locPrompt + promptTokens
                preferences[PreferencesKeys.LOCAL_COMPLETION_TOKENS] = locComp + completionTokens

                val isLocalWhisper = provider.contains("Whisper", ignoreCase = true) || model.contains("whisper", ignoreCase = true)
                if (isLocalWhisper) {
                    val wCalls = preferences[PreferencesKeys.LOCAL_WHISPER_CALLS_COUNT] ?: 0L
                    preferences[PreferencesKeys.LOCAL_WHISPER_CALLS_COUNT] = wCalls + 1

                    preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_TOKENS] = totalForCall
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_PROMPT_TOKENS] = promptTokens
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_COMPLETION_TOKENS] = completionTokens
                    if (latencyMs > 0L) preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_LATENCY_MS] = latencyMs
                    if (statusCode.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_STATUS_CODE] = statusCode
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_PROVIDER] = provider.ifBlank { "On-Device Whisper Base" }
                    if (model.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_LOCAL_WHISPER_MODEL] = model
                } else {
                    val gCalls = preferences[PreferencesKeys.LOCAL_GEMMA_CALLS_COUNT] ?: 0L
                    preferences[PreferencesKeys.LOCAL_GEMMA_CALLS_COUNT] = gCalls + 1

                    preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_TOKENS] = totalForCall
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_PROMPT_TOKENS] = promptTokens
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_COMPLETION_TOKENS] = completionTokens
                    if (latencyMs > 0L) preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_LATENCY_MS] = latencyMs
                    if (statusCode.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_STATUS_CODE] = statusCode
                    preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_PROVIDER] = provider.ifBlank { "On-Device Gemma 2B" }
                    if (model.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_LOCAL_GEMMA_MODEL] = model
                }
            }

            // Per-provider specific metrics
            if (provider.contains("Groq", ignoreCase = true)) {
                val grTotal = preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] ?: 0L
                val grPrompt = preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] ?: 0L
                val grComp = preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] ?: 0L
                val grCalls = preferences[PreferencesKeys.GROQ_CALLS_COUNT] ?: 0L

                preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] = grTotal + totalForCall
                preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] = grPrompt + promptTokens
                preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] = grComp + completionTokens
                preferences[PreferencesKeys.GROQ_CALLS_COUNT] = grCalls + 1

                val isWhisper = provider.contains("Whisper", ignoreCase = true) || model.contains("whisper", ignoreCase = true)
                if (isWhisper) {
                    val wTotal = preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] ?: 0L
                    val wCalls = preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] ?: 0L
                    val wAudio = preferences[PreferencesKeys.GROQ_WHISPER_AUDIO_SECONDS] ?: 0L
                    val wWords = preferences[PreferencesKeys.GROQ_WHISPER_OUTPUT_WORDS] ?: 0L
                    preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] = wTotal + totalForCall
                    preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] = wCalls + 1
                    if (audioSeconds > 0L) {
                        preferences[PreferencesKeys.GROQ_WHISPER_AUDIO_SECONDS] = wAudio + audioSeconds
                        preferences[PreferencesKeys.LAST_CALL_WHISPER_AUDIO_SECONDS] = audioSeconds
                    }
                    if (completionTokens > 0L) {
                        preferences[PreferencesKeys.GROQ_WHISPER_OUTPUT_WORDS] = wWords + completionTokens
                        preferences[PreferencesKeys.LAST_CALL_WHISPER_OUTPUT_WORDS] = completionTokens
                    }

                    preferences[PreferencesKeys.LAST_CALL_WHISPER_TOKENS] = totalForCall
                    preferences[PreferencesKeys.LAST_CALL_WHISPER_PROMPT_TOKENS] = promptTokens
                    preferences[PreferencesKeys.LAST_CALL_WHISPER_COMPLETION_TOKENS] = completionTokens
                    if (latencyMs > 0L) preferences[PreferencesKeys.LAST_CALL_WHISPER_LATENCY_MS] = latencyMs
                    if (statusCode.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_WHISPER_STATUS_CODE] = statusCode
                    preferences[PreferencesKeys.LAST_CALL_WHISPER_PROVIDER] = provider.ifBlank { "Groq LPU ~ Whisper" }
                    if (model.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_WHISPER_MODEL] = model
                } else {
                    val qTotal = preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] ?: 0L
                    val qPrompt = preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] ?: 0L
                    val qComp = preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] ?: 0L
                    val qCalls = preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] ?: 0L
                    preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] = qTotal + totalForCall
                    preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] = qPrompt + promptTokens
                    preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] = qComp + completionTokens
                    preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] = qCalls + 1

                    preferences[PreferencesKeys.LAST_CALL_QWEN_TOKENS] = totalForCall
                    preferences[PreferencesKeys.LAST_CALL_QWEN_PROMPT_TOKENS] = promptTokens
                    preferences[PreferencesKeys.LAST_CALL_QWEN_COMPLETION_TOKENS] = completionTokens
                    if (latencyMs > 0L) preferences[PreferencesKeys.LAST_CALL_QWEN_LATENCY_MS] = latencyMs
                    if (statusCode.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_QWEN_STATUS_CODE] = statusCode
                    preferences[PreferencesKeys.LAST_CALL_QWEN_PROVIDER] = provider.ifBlank { "Groq LPU ~ Qwen" }
                    if (model.isNotBlank()) preferences[PreferencesKeys.LAST_CALL_QWEN_MODEL] = model
                }
            }
        }
    }

    fun getDayWiseConsumption(engineType: String) = tokenUsageDao.getDayWiseConsumption(engineType)
    fun getMonthWiseConsumption(engineType: String) = tokenUsageDao.getMonthWiseConsumption(engineType)
    fun getRecentLogs(engineType: String, limit: Int = 100) = tokenUsageDao.getRecentLogs(engineType, limit)

    suspend fun resetTokenStats() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_TOKENS_USED] = 0L
            preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] = 0L

            preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_CALLS_COUNT] = 0L
            preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] = 0L
            preferences[PreferencesKeys.GROQ_WHISPER_AUDIO_SECONDS] = 0L
            preferences[PreferencesKeys.LAST_CALL_WHISPER_AUDIO_SECONDS] = 0L
            preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] = 0L

            preferences[PreferencesKeys.LOCAL_TOTAL_TOKENS] = 0L
            preferences[PreferencesKeys.LOCAL_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.LOCAL_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.LOCAL_GEMMA_CALLS_COUNT] = 0L
            preferences[PreferencesKeys.LOCAL_WHISPER_CALLS_COUNT] = 0L
        }
    }

    suspend fun setGroqWhisperAudioSeconds(seconds: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROQ_WHISPER_AUDIO_SECONDS] = seconds
        }
    }

    suspend fun setGroqWhisperOutputWords(words: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROQ_WHISPER_OUTPUT_WORDS] = words
        }
    }
}
