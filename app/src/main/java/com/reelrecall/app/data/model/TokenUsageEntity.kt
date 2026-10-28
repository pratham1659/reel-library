package com.reelrecall.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_usage_logs")
data class TokenUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // e.g., "2026-10-28"
    val monthString: String, // e.g., "2026-10"
    val engineType: String, // "CLOUD" or "LOCAL"
    val provider: String, // "Groq LPU (Whisper)", "Google Gemini (LLM)", "Local Whisper", "Local Gemma"
    val promptTokens: Long = 0L,
    val completionTokens: Long = 0L,
    val totalTokens: Long = 0L,
    val latencyMs: Long = 0L,
    val statusCode: String = "200 OK",
    val errorDetails: String? = null,
    val payloadSnippet: String? = null
)

data class TokenPeriodAggregate(
    val period: String,
    val totalTokens: Long,
    val promptTokens: Long,
    val completionTokens: Long,
    val requestCount: Int,
    val avgLatencyMs: Long
)
