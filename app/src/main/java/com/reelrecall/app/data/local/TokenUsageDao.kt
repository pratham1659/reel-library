package com.reelrecall.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.reelrecall.app.data.model.TokenPeriodAggregate
import com.reelrecall.app.data.model.TokenUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TokenUsageEntity): Long

    @Query(
        """
        SELECT 
            dateString AS period,
            SUM(totalTokens) AS totalTokens,
            SUM(promptTokens) AS promptTokens,
            SUM(completionTokens) AS completionTokens,
            COUNT(*) AS requestCount,
            CAST(AVG(latencyMs) AS INTEGER) AS avgLatencyMs
        FROM token_usage_logs
        WHERE engineType = :engineType
        GROUP BY dateString
        ORDER BY dateString DESC
        """
    )
    fun getDayWiseConsumption(engineType: String): Flow<List<TokenPeriodAggregate>>

    @Query(
        """
        SELECT 
            monthString AS period,
            SUM(totalTokens) AS totalTokens,
            SUM(promptTokens) AS promptTokens,
            SUM(completionTokens) AS completionTokens,
            COUNT(*) AS requestCount,
            CAST(AVG(latencyMs) AS INTEGER) AS avgLatencyMs
        FROM token_usage_logs
        WHERE engineType = :engineType
        GROUP BY monthString
        ORDER BY monthString DESC
        """
    )
    fun getMonthWiseConsumption(engineType: String): Flow<List<TokenPeriodAggregate>>

    @Query(
        """
        SELECT * FROM token_usage_logs
        WHERE engineType = :engineType
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun getRecentLogs(engineType: String, limit: Int = 50): Flow<List<TokenUsageEntity>>

    @Query("DELETE FROM token_usage_logs")
    suspend fun clearAll()
}
