package com.reelrecall.app.data.security

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class SecureKeyVaultTest {

    private lateinit var mockContext: Context
    private val fakeStores = mutableMapOf<String, InMemorySharedPreferences>()

    @Before
    fun setup() {
        fakeStores.clear()
        SecureKeyVault.resetInstanceForTesting()
        mockContext = TestContext { name ->
            fakeStores.getOrPut(name) { InMemorySharedPreferences() }
        }
    }

    private fun getFakeStore(name: String): InMemorySharedPreferences {
        return fakeStores.getOrPut(name) { InMemorySharedPreferences() }
    }

    // =========================================================================
    // 1. Keystore Unavailable / Failure Tests (RAM-only fallback)
    // =========================================================================

    @Test
    fun testStorageWhenAndroidKeystoreFails_usesInMemoryFallback() {
        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { null }, // Simulates Keystore exception
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        // 1. Initially empty
        assertEquals("", vault.getGroqApiKey())

        // 2. Setting key stores in in-memory session cache
        vault.setGroqApiKey("gsk_test_ram_only_key_123")
        assertEquals("gsk_test_ram_only_key_123", vault.getGroqApiKey())
        assertEquals("gsk_test_ram_only_key_123", vault.inMemoryFallback[SecureKeyVault.KEY_GROQ_API])

        // 3. Deleting key clears memory
        vault.deleteGroqApiKey()
        assertEquals("", vault.getGroqApiKey())
        assertNull(vault.inMemoryFallback[SecureKeyVault.KEY_GROQ_API])
    }

    // =========================================================================
    // 2. Verify Keys Are Never Persisted in Plaintext
    // =========================================================================

    @Test
    fun testKeysAreNeverPersistedInPlaintextWhenKeystoreFails() {
        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { null }, // Keystore failed
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        vault.setGroqApiKey("gsk_secret_not_on_disk_456")

        // Assert no plaintext SharedPreferences were written to on disk
        val libraryFallback = getFakeStore("reel_library_secure_fallback")
        val recallFallback = getFakeStore("reel_recall_secure_fallback")
        assertEquals("", libraryFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
        assertEquals("", recallFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
        assertTrue(libraryFallback.getAll().isEmpty())
        assertTrue(recallFallback.getAll().isEmpty())
    }

    // =========================================================================
    // 3. Confirm RAM-only Keys Are Cleared on Restart (Documented behavior)
    // =========================================================================

    @Test
    fun testRamOnlyKeysAreClearedOnRestart() {
        // App Session 1
        val session1Vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { null },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )
        session1Vault.setGroqApiKey("gsk_ephemeral_key_789")
        assertEquals("gsk_ephemeral_key_789", session1Vault.getGroqApiKey())

        // App Restart (Process termination & recreation: new vault instance with empty RAM)
        val session2Vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { null },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        // Verifies RAM-only keys do NOT survive process recreation.
        // Users on hardware without Keystore support must re-enter keys after restart.
        assertEquals("", session2Vault.getGroqApiKey())
    }

    // =========================================================================
    // 4. Test Migration from Every Supported Legacy Format
    // =========================================================================

    @Test
    fun testMigrationFromLegacyEncryptedVault() {
        val legacyEncrypted = getFakeStore("reel_recall_vault_encrypted")
        legacyEncrypted.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_legacy_encrypted_999").commit()

        val targetEncrypted = getFakeStore(SecureKeyVault.PREFS_FILENAME)

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { targetEncrypted },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        // Verifies key was migrated into target encrypted prefs
        assertEquals("gsk_legacy_encrypted_999", vault.getGroqApiKey())
        assertEquals("gsk_legacy_encrypted_999", targetEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))
        assertTrue(targetEncrypted.getBoolean(SecureKeyVault.KEY_MIGRATION_DONE, false))

        // Verifies legacy encrypted source was wiped to prevent ghost resurrection
        assertEquals("", legacyEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))
    }

    @Test
    fun testMigrationFromLegacyLibraryPlaintextFallback() {
        val legacyFallback = getFakeStore("reel_library_secure_fallback")
        legacyFallback.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_fallback_lib_555").commit()

        val targetEncrypted = getFakeStore(SecureKeyVault.PREFS_FILENAME)

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { targetEncrypted },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        assertEquals("gsk_fallback_lib_555", vault.getGroqApiKey())
        assertEquals("gsk_fallback_lib_555", targetEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))

        // Legacy plaintext fallback must be cleared
        assertEquals("", legacyFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
    }

    @Test
    fun testMigrationFromLegacyRecallPlaintextFallback() {
        val legacyRecallFallback = getFakeStore("reel_recall_secure_fallback")
        legacyRecallFallback.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_fallback_recall_333").commit()

        val targetEncrypted = getFakeStore(SecureKeyVault.PREFS_FILENAME)

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { targetEncrypted },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        assertEquals("gsk_fallback_recall_333", vault.getGroqApiKey())
        assertEquals("gsk_fallback_recall_333", targetEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))

        // Legacy plaintext fallback must be cleared
        assertEquals("", legacyRecallFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
    }

    // =========================================================================
    // 5. Failed Migration Cannot Delete Valid Source Key
    // =========================================================================

    @Test
    fun testFailedMigrationCannotDeleteValidSourceKey() {
        val legacyStore = getFakeStore("reel_recall_vault_encrypted")
        legacyStore.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_valuable_production_key").commit()

        // Create failing target prefs where commit() always fails
        val failingTarget = FailingCommitSharedPreferences()

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { failingTarget },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        // Trigger safe migration
        vault.performSafeMigration(failingTarget)

        // Critical check: Source legacy key MUST NOT be wiped if target commit failed
        assertEquals("gsk_valuable_production_key", legacyStore.getString(SecureKeyVault.KEY_GROQ_API, ""))
    }

    // =========================================================================
    // 6. Deletion Clears Encrypted, Legacy, and In-Memory Copies
    // =========================================================================

    @Test
    fun testDeletionClearsEncryptedLegacyAndInMemoryCopies() {
        val targetEncrypted = getFakeStore(SecureKeyVault.PREFS_FILENAME)
        val legacyEncrypted = getFakeStore("reel_recall_vault_encrypted")
        val legacyLibFallback = getFakeStore("reel_library_secure_fallback")
        val legacyRecallFallback = getFakeStore("reel_recall_secure_fallback")

        targetEncrypted.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_key_to_delete").commit()
        legacyEncrypted.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_key_to_delete").commit()
        legacyLibFallback.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_key_to_delete").commit()
        legacyRecallFallback.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_key_to_delete").commit()

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { targetEncrypted },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )
        vault.inMemoryFallback[SecureKeyVault.KEY_GROQ_API] = "gsk_key_to_delete"

        // Execute deletion
        vault.deleteGroqApiKey()

        // 1. Target encrypted is empty
        assertEquals("", targetEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))
        // 2. In-memory cache is empty
        assertNull(vault.inMemoryFallback[SecureKeyVault.KEY_GROQ_API])
        // 3. Legacy encrypted is empty
        assertEquals("", legacyEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))
        // 4. Legacy fallbacks are empty
        assertEquals("", legacyLibFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
        assertEquals("", legacyRecallFallback.getString(SecureKeyVault.KEY_GROQ_API, ""))
        // 5. Vault getter returns empty
        assertEquals("", vault.getGroqApiKey())
    }

    // =========================================================================
    // 7. Startup Cleanup Only Removes Intended Legacy Files
    // =========================================================================

    @Test
    fun testStartupCleanupOnlyRemovesIntendedLegacyFiles() {
        val targetEncrypted = getFakeStore(SecureKeyVault.PREFS_FILENAME)
        targetEncrypted.edit().putString(SecureKeyVault.KEY_GROQ_API, "gsk_active_vault_key").commit()

        val legacyLib = getFakeStore("reel_library_secure_fallback")
        legacyLib.edit().putString("stale_key", "stale_val").commit()

        val legacyRecall = getFakeStore("reel_recall_secure_fallback")
        legacyRecall.edit().putString("old_key", "old_val").commit()

        val unrelatedStore = getFakeStore("unrelated_user_preferences")
        unrelatedStore.edit().putString("user_theme", "DARK_MODE").commit()

        val vault = SecureKeyVault.createForTesting(
            context = mockContext,
            encryptedPrefsSupplier = { targetEncrypted },
            legacyPrefsResolver = { name -> getFakeStore(name) }
        )

        vault.cleanupLegacyPlaintextFiles()

        // Intended legacy stores are wiped
        assertTrue(legacyLib.getAll().isEmpty())
        assertTrue(legacyRecall.getAll().isEmpty())

        // Target encrypted and unrelated preferences are completely preserved
        assertEquals("gsk_active_vault_key", targetEncrypted.getString(SecureKeyVault.KEY_GROQ_API, ""))
        assertEquals("DARK_MODE", unrelatedStore.getString("user_theme", ""))
    }
}

// =============================================================================
// Lightweight In-Memory SharedPreferences implementation for Unit Testing
// =============================================================================

class InMemorySharedPreferences : SharedPreferences {
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

    override fun edit(): SharedPreferences.Editor = EditorImpl(map)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class EditorImpl(private val backingMap: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val staged = mutableMapOf<String, Any?>()
        private val toRemove = mutableSetOf<String>()
        private var clearStaged = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) {
                staged[key] = value
                toRemove.remove(key)
            }
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            if (key != null) staged[key] = values
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) staged[key] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) staged[key] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) staged[key] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) staged[key] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) {
                toRemove.add(key)
                staged.remove(key)
            }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearStaged = true
            return this
        }

        override fun commit(): Boolean {
            if (clearStaged) {
                backingMap.clear()
            }
            toRemove.forEach { backingMap.remove(it) }
            staged.forEach { (k, v) ->
                if (v == null) backingMap.remove(k) else backingMap[k] = v
            }
            return true
        }

        override fun apply() {
            commit()
        }
    }
}

class FailingCommitSharedPreferences : SharedPreferences by InMemorySharedPreferences() {
    override fun edit(): SharedPreferences.Editor {
        return object : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?) = this
            override fun putStringSet(key: String?, values: MutableSet<String>?) = this
            override fun putInt(key: String?, value: Int) = this
            override fun putLong(key: String?, value: Long) = this
            override fun putFloat(key: String?, value: Float) = this
            override fun putBoolean(key: String?, value: Boolean) = this
            override fun remove(key: String?) = this
            override fun clear() = this
            override fun commit(): Boolean = false // Always fails
            override fun apply() {}
        }
    }
}

class TestContext(private val prefsProvider: (String) -> SharedPreferences) : android.content.ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return prefsProvider(name ?: "default")
    }
}

