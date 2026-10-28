package com.reelrecall.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.ReelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {

    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Query("""
        SELECT reels.* FROM reels
        JOIN reels_fts ON reels.rowid = reels_fts.docid
        WHERE reels_fts MATCH :searchQuery
        ORDER BY reels.createdAt DESC
    """)
    fun searchReelsFts(searchQuery: String): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE id = :id")
    fun getReelById(id: Long): Flow<ReelEntity?>

    @Query("SELECT * FROM reels WHERE canonicalUrl = :canonicalUrl LIMIT 1")
    suspend fun findByCanonicalUrl(canonicalUrl: String): ReelEntity?

    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    suspend fun getAllReelsSync(): List<ReelEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reel: ReelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reels: List<ReelEntity>): List<Long>

    @Update
    suspend fun update(reel: ReelEntity)

    @Query("UPDATE reels SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: Long, status: IngestionStatus, errorMessage: String? = null)

    @Query("UPDATE reels SET userNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    @Query("UPDATE reels SET tagsJson = :tagsJson WHERE id = :id")
    suspend fun updateTags(id: Long, tagsJson: String)

    @Query("DELETE FROM reels WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reels WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM reels")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM reels")
    fun getReelCount(): Flow<Int>

}
