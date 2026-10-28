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
        displayName = "Cloud Free-Tier (Groq + Gemini)",
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
    val cloudBackupFolder: String = "ReelRecall_Vault",
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
    val lastCallProvider: String = ""
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
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        val modeString = preferences[PreferencesKeys.ENGINE_MODE] ?: EngineMode.CLOUD_ONLY.name
        val mode = try {
            EngineMode.valueOf(modeString)
        } catch (e: Exception) {
            EngineMode.CLOUD_ONLY
        }

        AppPreferences(
            engineMode = mode,
            groqApiKey = preferences[PreferencesKeys.GROQ_API_KEY] ?: "",
            geminiApiKey = preferences[PreferencesKeys.GEMINI_API_KEY] ?: "",
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
            cloudBackupFolder = preferences[PreferencesKeys.CLOUD_BACKUP_FOLDER] ?: "ReelRecall_Vault",
            lastBackupTimeFormatted = preferences[PreferencesKeys.LAST_BACKUP_TIME] ?: "Never backed up",
            totalTokensUsed = preferences[PreferencesKeys.TOTAL_TOKENS_USED] ?: 0L,
            totalPromptTokens = preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] ?: 0L,
            totalCompletionTokens = preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] ?: 0L,
            lastCallTokens = preferences[PreferencesKeys.LAST_CALL_TOKENS] ?: 0L,
            lastCallPromptTokens = preferences[PreferencesKeys.LAST_CALL_PROMPT_TOKENS] ?: 0L,
            lastCallCompletionTokens = preferences[PreferencesKeys.LAST_CALL_COMPLETION_TOKENS] ?: 0L,
            lastCallLatencyMs = preferences[PreferencesKeys.LAST_CALL_LATENCY_MS] ?: 0L,
            lastCallStatusCode = preferences[PreferencesKeys.LAST_CALL_STATUS_CODE] ?: "",
            lastCallProvider = preferences[PreferencesKeys.LAST_CALL_PROVIDER] ?: ""
        )
    }

    suspend fun setEngineMode(mode: EngineMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENGINE_MODE] = mode.name
        }
    }

    suspend fun setApiKeys(groqKey: String, geminiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROQ_API_KEY] = groqKey.trim()
            preferences[PreferencesKeys.GEMINI_API_KEY] = geminiKey.trim()
        }
    }

    suspend fun setGroqApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROQ_API_KEY] = key.trim()
        }
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = key.trim()
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

    suspend fun recordTokenUsage(
        promptTokens: Long,
        completionTokens: Long,
        latencyMs: Long = 0L,
        statusCode: String = "200 OK",
        provider: String = ""
    ) {
        if (promptTokens <= 0L && completionTokens <= 0L) return
        context.dataStore.edit { preferences ->
            val currentPrompt = preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] ?: 0L
            val currentComp = preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] ?: 0L
            val currentTotal = preferences[PreferencesKeys.TOTAL_TOKENS_USED] ?: 0L
            val totalForCall = promptTokens + completionTokens

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
        }
    }

    suspend fun resetTokenStats() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_TOKENS_USED] = 0L
            preferences[PreferencesKeys.TOTAL_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.TOTAL_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.LAST_CALL_TOKENS] = 0L
            preferences[PreferencesKeys.LAST_CALL_PROMPT_TOKENS] = 0L
            preferences[PreferencesKeys.LAST_CALL_COMPLETION_TOKENS] = 0L
            preferences[PreferencesKeys.LAST_CALL_LATENCY_MS] = 0L
            preferences[PreferencesKeys.LAST_CALL_STATUS_CODE] = ""
            preferences[PreferencesKeys.LAST_CALL_PROVIDER] = ""
        }
    }
}
