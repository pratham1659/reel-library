package com.reelrecall.app.data.model

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full-Text Search virtual table backing instant sub-millisecond keyword queries
 * across titles, summaries, takeaways, captions, and transcripts.
 */
@Entity(tableName = "reels_fts")
@Fts4(contentEntity = ReelEntity::class)
data class ReelFtsEntity(
    val title: String?,
    val oneLineSummary: String?,
    val takeawaysJson: String,
    val caption: String?,
    val transcript: String?,
    val tagsJson: String
)
