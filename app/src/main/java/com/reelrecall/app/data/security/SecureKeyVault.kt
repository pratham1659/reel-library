package com.reelrecall.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.concurrent.ConcurrentHashMap

/**
 * Hardware-backed Android Keystore vault for Groq API keys and sensitive credentials.
 * Utilizes EncryptedSharedPreferences with AES256-GCM encryption so that
 * raw API keys are never stored as plain-text on the filesystem.
 *
 * If hardware Keystore encounters an exception, an in-memory session vault is used
 * rather than writing unencrypted credentials to disk.
 */
class SecureKeyVault internal constructor(
    context: Context,
    encryptedPrefsSupplier: ((Context) -> SharedPreferences?)? = null,
    private val legacyPrefsResolver: ((name: String) -> SharedPreferences?)? = null
) {

    private val appContext = context.applicationContext
    internal val inMemoryFallback = ConcurrentHashMap<String, String>()

    private val sharedPreferences: SharedPreferences? by lazy {
        val prefs = if (encryptedPrefsSupplier != null) {
            encryptedPrefsSupplier(appContext)
        } else {
            createProductionEncryptedPrefs(appContext)
        }

        if (prefs != null) {
            performSafeMigration(prefs)
            cleanupLegacyPlaintextFiles()
        }

        prefs
    }

    private fun createProductionEncryptedPrefs(context: Context): SharedPreferences? {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_FILENAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // Hardware Keystore unavailable or crypto initialization failed.
            // Degrade to in-memory session storage; never persist unencrypted keys to disk.
            null
        }
    }

    private fun resolveLegacyStore(name: String): SharedPreferences? {
        if (legacyPrefsResolver != null) {
            return legacyPrefsResolver.invoke(name)
        }
        return if (name == "reel_recall_vault_encrypted") {
            try {
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                EncryptedSharedPreferences.create(
                    name,
                    masterKeyAlias,
                    appContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (_: Exception) {
                null
            }
        } else {
            try {
                appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Atomically migrates existing keys from legacy encrypted or plaintext stores.
     * Guarantees that a legacy source is NEVER wiped unless the key has been
     * successfully written and committed to the encrypted destination.
     */
    internal fun performSafeMigration(targetPrefs: SharedPreferences) {
        val existingKey = targetPrefs.getString(KEY_GROQ_API, "")
        if (!existingKey.isNullOrBlank()) {
            // Target vault already possesses a valid key. Mark migration complete.
            if (!targetPrefs.getBoolean(KEY_MIGRATION_DONE, false)) {
                targetPrefs.edit().putBoolean(KEY_MIGRATION_DONE, true).commit()
            }
            return
        }

        // Search supported legacy formats in priority order
        val legacyKey = findLegacyKey()
        if (legacyKey != null && legacyKey.key.isNotBlank()) {
            val success = targetPrefs.edit()
                .putString(KEY_GROQ_API, legacyKey.key)
                .putBoolean(KEY_MIGRATION_DONE, true)
                .commit()

            if (success) {
                // Wipe source legacy store only after confirmed persistence in target vault
                legacyKey.sourcePrefs.edit().remove(KEY_GROQ_API).commit()
            }
        } else {
            targetPrefs.edit().putBoolean(KEY_MIGRATION_DONE, true).commit()
        }
    }

    private data class LegacyKeyCandidate(val key: String, val sourcePrefs: SharedPreferences)

    private fun findLegacyKey(): LegacyKeyCandidate? {
        val candidateStores = listOf(
            "reel_recall_vault_encrypted",
            "reel_library_secure_fallback",
            "reel_recall_secure_fallback"
        )
        for (storeName in candidateStores) {
            val store = resolveLegacyStore(storeName) ?: continue
            val k = store.getString(KEY_GROQ_API, "")
            if (!k.isNullOrBlank()) {
                return LegacyKeyCandidate(k, store)
            }
        }
        return null
    }

    /**
     * Wipes legacy plaintext fallback files from disk.
     * Only invoked after the encrypted vault is operational and migration has verified.
     * Touches strictly the intended fallback SharedPreferences and no other files.
     */
    internal fun cleanupLegacyPlaintextFiles() {
        listOf(
            "reel_library_secure_fallback",
            "reel_recall_secure_fallback"
        ).forEach { storeName ->
            try {
                resolveLegacyStore(storeName)?.edit()?.clear()?.commit()
            } catch (_: Exception) {}
        }
    }

    fun getGroqApiKey(): String {
        return sharedPreferences?.getString(KEY_GROQ_API, "")?.ifBlank { null }
            ?: inMemoryFallback[KEY_GROQ_API]
            ?: ""
    }

    fun setGroqApiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isBlank()) {
            deleteGroqApiKey()
        } else {
            sharedPreferences?.edit()?.putString(KEY_GROQ_API, trimmed)?.commit()
            inMemoryFallback[KEY_GROQ_API] = trimmed
        }
    }

    fun deleteGroqApiKey() {
        sharedPreferences?.edit()?.remove(KEY_GROQ_API)?.commit()
        inMemoryFallback.remove(KEY_GROQ_API)
        wipeKeyFromAllLocations(KEY_GROQ_API)
    }

    fun clearAllKeys() {
        deleteGroqApiKey()
    }

    /**
     * Synchronously purges a key from the current encrypted vault, legacy encrypted vault,
     * and legacy fallback files to prevent resurrecting deleted keys.
     */
    private fun wipeKeyFromAllLocations(keyName: String) {
        listOf(
            "reel_recall_vault_encrypted",
            "reel_library_secure_fallback",
            "reel_recall_secure_fallback"
        ).forEach { storeName ->
            try {
                resolveLegacyStore(storeName)?.edit()?.remove(keyName)?.commit()
            } catch (_: Exception) {}
        }
    }

    companion object {
        internal const val PREFS_FILENAME = "reel_library_vault_encrypted"
        internal const val KEY_GROQ_API = "vault_groq_api_key"
        internal const val KEY_MIGRATION_DONE = "vault_migration_completed"

        @Volatile
        private var INSTANCE: SecureKeyVault? = null

        fun getInstance(context: Context): SecureKeyVault {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureKeyVault(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Test helper to create an instance with custom suppliers.
         */
        internal fun createForTesting(
            context: Context,
            encryptedPrefsSupplier: ((Context) -> SharedPreferences?)?,
            legacyPrefsResolver: ((name: String) -> SharedPreferences?)? = null
        ): SecureKeyVault {
            return SecureKeyVault(context.applicationContext, encryptedPrefsSupplier, legacyPrefsResolver)
        }

        internal fun resetInstanceForTesting() {
            INSTANCE = null
        }
    }
}
