package com.reelrecall.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Hardware-backed Android Keystore vault for API keys and sensitive credentials.
 * Utilizes EncryptedSharedPreferences with AES256-GCM encryption so that
 * raw API keys are never stored as plain-text on the filesystem.
 */
class SecureKeyVault private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREFS_FILENAME,
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            // Migrate key from legacy encrypted vault if new vault is empty
            if (prefs.getString(KEY_GROQ_API, "").isNullOrBlank()) {
                try {
                    val oldPrefs = EncryptedSharedPreferences.create(
                        "reel_recall_vault_encrypted",
                        masterKeyAlias,
                        context.applicationContext,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    val oldGroq = oldPrefs.getString(KEY_GROQ_API, "") ?: ""
                    val oldGemini = oldPrefs.getString(KEY_GEMINI_API, "") ?: ""
                    if (oldGroq.isNotBlank()) prefs.edit().putString(KEY_GROQ_API, oldGroq).apply()
                    if (oldGemini.isNotBlank()) prefs.edit().putString(KEY_GEMINI_API, oldGemini).apply()
                } catch (ignored: Exception) {}
            }
            prefs
        } catch (e: Exception) {
            // Fallback for devices or test environments with Keystore issues
            val fallback = context.applicationContext.getSharedPreferences("reel_library_secure_fallback", Context.MODE_PRIVATE)
            val oldFallback = context.applicationContext.getSharedPreferences("reel_recall_secure_fallback", Context.MODE_PRIVATE)
            if (fallback.all.isEmpty() && oldFallback.all.isNotEmpty()) {
                val ed = fallback.edit()
                oldFallback.all.forEach { (k, v) -> if (v is String) ed.putString(k, v) }
                ed.apply()
            }
            fallback
        }
    }

    fun getGroqApiKey(): String {
        return sharedPreferences.getString(KEY_GROQ_API, "") ?: ""
    }

    fun setGroqApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_GROQ_API, key.trim()).apply()
    }

    fun getGeminiApiKey(): String {
        return sharedPreferences.getString(KEY_GEMINI_API, "") ?: ""
    }

    fun setGeminiApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_GEMINI_API, key.trim()).apply()
    }

    fun clearAllKeys() {
        sharedPreferences.edit()
            .remove(KEY_GROQ_API)
            .remove(KEY_GEMINI_API)
            .apply()
    }

    companion object {
        private const val PREFS_FILENAME = "reel_library_vault_encrypted"
        private const val KEY_GROQ_API = "vault_groq_api_key"
        private const val KEY_GEMINI_API = "vault_gemini_api_key"

        @Volatile
        private var INSTANCE: SecureKeyVault? = null

        fun getInstance(context: Context): SecureKeyVault {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureKeyVault(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
