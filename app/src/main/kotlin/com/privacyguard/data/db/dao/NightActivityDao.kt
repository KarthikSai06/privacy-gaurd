package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.NightActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface NightActivityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(activity: NightActivity): Long

    @Query("SELECT * FROM night_activities ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NightActivity>>

    @Query("SELECT * FROM night_activities WHERE date >= :fromDate ORDER BY timestamp DESC")
    fun getFrom(fromDate: Long): Flow<List<NightActivity>>

    @Query("SELECT COUNT(*) FROM night_activities WHERE date >= :weekAgo")
    fun countThisWeek(weekAgo: Long): Flow<Int>

    @Query("""
        SELECT * FROM night_activities 
        WHERE packageName = :pkg AND date = :date LIMIT 1
    """)
    suspend fun findExisting(pkg: String, date: Long): NightActivity?

    @Query("DELETE FROM night_activities WHERE date < :before")
    suspend fun deleteOlderThan(before: Long)
}
