package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.AppLocationUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationUsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usage: AppLocationUsage): Long

    @Query("SELECT * FROM app_location_usage WHERE date >= :since ORDER BY lastAccessTime DESC")
    fun getUsageSince(since: Long): Flow<List<AppLocationUsage>>

    @Query("SELECT * FROM app_location_usage ORDER BY lastAccessTime DESC")
    fun getAllUsage(): Flow<List<AppLocationUsage>>

    @Query("SELECT COUNT(DISTINCT packageName) FROM app_location_usage WHERE date >= :todayMidnight")
    fun countTodayApps(todayMidnight: Long): Flow<Int>

    @Query("SELECT * FROM app_location_usage WHERE packageName = :pkg ORDER BY lastAccessTime DESC")
    fun getUsageForPackage(pkg: String): Flow<List<AppLocationUsage>>

    @Query("DELETE FROM app_location_usage WHERE date < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM app_location_usage WHERE packageName = :pkg AND date = :date LIMIT 1")
    suspend fun findExisting(pkg: String, date: Long): AppLocationUsage?

    @Update
    suspend fun update(usage: AppLocationUsage)
}
