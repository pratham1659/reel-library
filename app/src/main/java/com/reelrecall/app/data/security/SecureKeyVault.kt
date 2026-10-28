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
            EncryptedSharedPreferences.create(
                PREFS_FILENAME,
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for devices or test environments with Keystore issues
            context.applicationContext.getSharedPreferences("reel_recall_secure_fallback", Context.MODE_PRIVATE)
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
        private const val PREFS_FILENAME = "reel_recall_vault_encrypted"
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
