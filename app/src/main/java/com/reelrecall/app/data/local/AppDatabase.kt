package com.reelrecall.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.data.model.ReelFtsEntity
import com.reelrecall.app.data.model.TokenUsageEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReelEntity::class, ReelFtsEntity::class, TokenUsageEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reelDao(): ReelDao
    abstract fun tokenUsageDao(): TokenUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reels ADD COLUMN userNotes TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS token_usage_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        dateString TEXT NOT NULL,
                        monthString TEXT NOT NULL,
                        engineType TEXT NOT NULL,
                        provider TEXT NOT NULL,
                        promptTokens INTEGER NOT NULL,
                        completionTokens INTEGER NOT NULL,
                        totalTokens INTEGER NOT NULL,
                        latencyMs INTEGER NOT NULL,
                        statusCode TEXT NOT NULL,
                        errorDetails TEXT,
                        payloadSnippet TEXT
                    )
                    """
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val oldDb = appContext.getDatabasePath("reel_recall.db")
                val newDb = appContext.getDatabasePath("reel_library.db")

                // Seamlessly migrate existing database files if old exists and new does not yet
                if (oldDb.exists() && !newDb.exists()) {
                    try {
                        newDb.parentFile?.mkdirs()
                        oldDb.copyTo(newDb, overwrite = true)
                        val oldWal = java.io.File(oldDb.parentFile, "reel_recall.db-wal")
                        val newWal = java.io.File(newDb.parentFile, "reel_library.db-wal")
                        if (oldWal.exists()) oldWal.copyTo(newWal, overwrite = true)
                        val oldShm = java.io.File(oldDb.parentFile, "reel_recall.db-shm")
                        val newShm = java.io.File(newDb.parentFile, "reel_library.db-shm")
                        if (oldShm.exists()) oldShm.copyTo(newShm, overwrite = true)
                        android.util.Log.i("AppDatabase", "Successfully migrated reel_recall.db to reel_library.db")
                    } catch (e: Exception) {
                        android.util.Log.e("AppDatabase", "Error migrating database files", e)
                    }
                }

                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "reel_library.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

