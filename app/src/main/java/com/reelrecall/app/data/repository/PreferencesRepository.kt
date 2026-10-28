package com.reelrecall.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "reel_recall_preferences")

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
    val geminiApiKey: String = "",
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
    val geminiTotalTokens: Long = 0L,
    val geminiPromptTokens: Long = 0L,
    val geminiCompletionTokens: Long = 0L,
    val geminiCallsCount: Long = 0L,
    val groqTotalTokens: Long = 0L,
    val groqPromptTokens: Long = 0L,
    val groqCompletionTokens: Long = 0L,
    val groqCallsCount: Long = 0L,
    val groqWhisperTokens: Long = 0L,
    val groqWhisperCalls: Long = 0L,
    val groqQwenTokens: Long = 0L,
    val groqQwenPromptTokens: Long = 0L,
    val groqQwenCompletionTokens: Long = 0L,
    val groqQwenCalls: Long = 0L
)

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ENGINE_MODE = stringPreferencesKey("engine_mode")
        val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
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
        val GEMINI_TOTAL_TOKENS = longPreferencesKey("gemini_total_tokens")
        val GEMINI_PROMPT_TOKENS = longPreferencesKey("gemini_prompt_tokens")
        val GEMINI_COMPLETION_TOKENS = longPreferencesKey("gemini_completion_tokens")
        val GEMINI_CALLS_COUNT = longPreferencesKey("gemini_calls_count")
        val GROQ_TOTAL_TOKENS = longPreferencesKey("groq_total_tokens")
        val GROQ_PROMPT_TOKENS = longPreferencesKey("groq_prompt_tokens")
        val GROQ_COMPLETION_TOKENS = longPreferencesKey("groq_completion_tokens")
        val GROQ_CALLS_COUNT = longPreferencesKey("groq_calls_count")
        val GROQ_WHISPER_TOTAL_TOKENS = longPreferencesKey("groq_whisper_total_tokens")
        val GROQ_WHISPER_CALLS_COUNT = longPreferencesKey("groq_whisper_calls_count")
        val GROQ_QWEN_TOTAL_TOKENS = longPreferencesKey("groq_qwen_total_tokens")
        val GROQ_QWEN_PROMPT_TOKENS = longPreferencesKey("groq_qwen_prompt_tokens")
        val GROQ_QWEN_COMPLETION_TOKENS = longPreferencesKey("groq_qwen_completion_tokens")
        val GROQ_QWEN_CALLS_COUNT = longPreferencesKey("groq_qwen_calls_count")
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

        // Retrieve securely from hardware-backed Keystore vault, fallback/migrate from DataStore
        val groqKey = secureKeyVault.getGroqApiKey().ifBlank {
            val legacyKey = preferences[PreferencesKeys.GROQ_API_KEY] ?: ""
            if (legacyKey.isNotBlank()) {
                secureKeyVault.setGroqApiKey(legacyKey)
            }
            legacyKey
        }
        val geminiKey = secureKeyVault.getGeminiApiKey().ifBlank {
            val legacyKey = preferences[PreferencesKeys.GEMINI_API_KEY] ?: ""
            if (legacyKey.isNotBlank()) {
                secureKeyVault.setGeminiApiKey(legacyKey)
            }
            legacyKey
        }

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

        AppPreferences(
            engineMode = mode,
            groqApiKey = groqKey,
            geminiApiKey = geminiKey,
            isWhisperModelDownloaded = preferences[PreferencesKeys.IS_WHISPER_DOWNLOADED] ?: false,
            isLlmModelDownloaded = preferences[PreferencesKeys.IS_LLM_DOWNLOADED] ?: false,
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
            geminiTotalTokens = preferences[PreferencesKeys.GEMINI_TOTAL_TOKENS] ?: 0L,
            geminiPromptTokens = preferences[PreferencesKeys.GEMINI_PROMPT_TOKENS] ?: 0L,
            geminiCompletionTokens = preferences[PreferencesKeys.GEMINI_COMPLETION_TOKENS] ?: 0L,
            geminiCallsCount = preferences[PreferencesKeys.GEMINI_CALLS_COUNT] ?: 0L,
            groqTotalTokens = preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] ?: 0L,
            groqPromptTokens = preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] ?: 0L,
            groqCompletionTokens = preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] ?: 0L,
            groqCallsCount = preferences[PreferencesKeys.GROQ_CALLS_COUNT] ?: 0L,
            groqWhisperTokens = preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] ?: 0L,
            groqWhisperCalls = preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] ?: 0L,
            groqQwenTokens = preferences[PreferencesKeys.GROQ_QWEN_TOTAL_TOKENS] ?: (preferences[PreferencesKeys.GROQ_TOTAL_TOKENS] ?: 0L),
            groqQwenPromptTokens = preferences[PreferencesKeys.GROQ_QWEN_PROMPT_TOKENS] ?: (preferences[PreferencesKeys.GROQ_PROMPT_TOKENS] ?: 0L),
            groqQwenCompletionTokens = preferences[PreferencesKeys.GROQ_QWEN_COMPLETION_TOKENS] ?: (preferences[PreferencesKeys.GROQ_COMPLETION_TOKENS] ?: 0L),
            groqQwenCalls = preferences[PreferencesKeys.GROQ_QWEN_CALLS_COUNT] ?: (preferences[PreferencesKeys.GROQ_CALLS_COUNT] ?: 0L)
        )
    }

    suspend fun setEngineMode(mode: EngineMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENGINE_MODE] = mode.name
        }
    }

    suspend fun setApiKeys(groqKey: String, geminiKey: String) {
        secureKeyVault.setGroqApiKey(groqKey)
        secureKeyVault.setGeminiApiKey(geminiKey)
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.GROQ_API_KEY)
            preferences.remove(PreferencesKeys.GEMINI_API_KEY)
        }
    }

    suspend fun setGroqApiKey(key: String) {
        secureKeyVault.setGroqApiKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.GROQ_API_KEY)
        }
    }

    suspend fun setGeminiApiKey(key: String) {
        secureKeyVault.setGeminiApiKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.GEMINI_API_KEY)
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
                preferences[PreferencesKeys.GEMINI_TOTAL_TOKENS] = 0L
                preferences[PreferencesKeys.GEMINI_PROMPT_TOKENS] = 0L
                preferences[PreferencesKeys.GEMINI_COMPLETION_TOKENS] = 0L
                preferences[PreferencesKeys.GEMINI_CALLS_COUNT] = 0L
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
        errorMessage: String? = null
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
                provider.contains("Local", ignoreCase = true)
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

            // Per-provider specific metrics
            if (provider.contains("Gemini", ignoreCase = true)) {
                val gTotal = preferences[PreferencesKeys.GEMINI_TOTAL_TOKENS] ?: 0L
                val gPrompt = preferences[PreferencesKeys.GEMINI_PROMPT_TOKENS] ?: 0L
                val gComp = preferences[PreferencesKeys.GEMINI_COMPLETION_TOKENS] ?: 0L
                val gCalls = preferences[PreferencesKeys.GEMINI_CALLS_COUNT] ?: 0L

                preferences[PreferencesKeys.GEMINI_TOTAL_TOKENS] = gTotal + totalForCall
                preferences[PreferencesKeys.GEMINI_PROMPT_TOKENS] = gPrompt + promptTokens
                preferences[PreferencesKeys.GEMINI_COMPLETION_TOKENS] = gComp + completionTokens
                preferences[PreferencesKeys.GEMINI_CALLS_COUNT] = gCalls + 1
            } else if (provider.contains("Groq", ignoreCase = true)) {
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
                    preferences[PreferencesKeys.GROQ_WHISPER_TOTAL_TOKENS] = wTotal + totalForCall
                    preferences[PreferencesKeys.GROQ_WHISPER_CALLS_COUNT] = wCalls + 1

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
            // Daily and aggregate counters reset, but LAST_CALL telemetry is preserved
            // so user can continuously monitor the most recent invocation

            preferences[PreferencesKeys.GEMINI_TOTAL_TOKENS] = 0L
            preferences[PreferencesKeys.GEMINI_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.GEMINI_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.GEMINI_CALLS_COUNT] = 0L

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
        }
    }
}
