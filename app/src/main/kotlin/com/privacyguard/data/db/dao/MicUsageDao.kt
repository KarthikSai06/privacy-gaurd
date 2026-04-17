package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.AppMicUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface MicUsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usage: AppMicUsage): Long

    @Query("SELECT * FROM app_mic_usage WHERE date >= :since ORDER BY lastAccessTime DESC")
    fun getUsageSince(since: Long): Flow<List<AppMicUsage>>

    @Query("SELECT * FROM app_mic_usage ORDER BY lastAccessTime DESC")
    fun getAllUsage(): Flow<List<AppMicUsage>>

    @Query("SELECT COUNT(DISTINCT packageName) FROM app_mic_usage WHERE date >= :todayMidnight")
    fun countTodayApps(todayMidnight: Long): Flow<Int>

    @Query("SELECT * FROM app_mic_usage WHERE packageName = :pkg ORDER BY lastAccessTime DESC")
    fun getUsageForPackage(pkg: String): Flow<List<AppMicUsage>>

    @Query("DELETE FROM app_mic_usage WHERE date < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM app_mic_usage WHERE packageName = :pkg AND date = :date LIMIT 1")
    suspend fun findExisting(pkg: String, date: Long): AppMicUsage?

    @Update
    suspend fun update(usage: AppMicUsage)
}
