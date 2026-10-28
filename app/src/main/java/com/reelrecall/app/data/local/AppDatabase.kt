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
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reel_recall.db"
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

