package com.reelrecall.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Ingestion state machine tracking background pipeline progress.
 */
@Serializable
enum class IngestionStatus {
    QUEUED,
    EXTRACTING,
    TRANSCRIBING,
    SUMMARIZING,
    COMPLETED,
    FAILED
}

/**
 * Primary persistent entity storing indexed Reel information.
 */
@Serializable
@Entity(
    tableName = "reels",
    indices = [
        Index(value = ["canonicalUrl"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class ReelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val canonicalUrl: String,
    val shortcode: String,
    val creatorHandle: String? = null,
    val caption: String? = null,
    val audioPath: String? = null,
    val thumbnailUrl: String? = null,
    val transcript: String? = null,
    val title: String? = null,

    val oneLineSummary: String? = null,
    val takeawaysJson: String = "[]", // Serialized List<String>
    val tagsJson: String = "[]",      // Serialized List<String>
    val engineUsed: String? = null,   // "Cloud (Groq LPU)" or "Local (Whisper + Gemma)"
    val status: IngestionStatus = IngestionStatus.QUEUED,
    val errorMessage: String? = null,
    val transcriptNote: String? = null,
    val userNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Universal JSON structure for Export & Import across platforms.
 */
@Serializable
data class ReelLibraryExport(
    val version: Int = 1,
    val exportDate: String,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val totalReels: Int,
    val reels: List<ReelEntity>
)

/**
 * Structured output schema for AI-generated takeaways.
 */
@Serializable
data class ReelSummary(
    val title: String,
    val one_line_summary: String,
    val actionable_takeaways: List<String>,
    val tags: List<String>
)
