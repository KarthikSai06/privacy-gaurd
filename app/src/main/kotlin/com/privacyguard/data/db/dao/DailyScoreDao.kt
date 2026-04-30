package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.DailyScore
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: DailyScore)

    @Query("SELECT * FROM daily_scores ORDER BY date DESC")
    fun getAll(): Flow<List<DailyScore>>

    @Query("SELECT * FROM daily_scores ORDER BY date DESC LIMIT :days")
    fun getRecent(days: Int = 30): Flow<List<DailyScore>>

    @Query("SELECT * FROM daily_scores WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyScore?

    @Query("DELETE FROM daily_scores")
    suspend fun deleteAll()
}
