package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.AppCameraUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraUsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usage: AppCameraUsage): Long

    @Query("SELECT * FROM app_camera_usage WHERE date >= :since ORDER BY lastAccessTime DESC")
    fun getUsageSince(since: Long): Flow<List<AppCameraUsage>>

    @Query("SELECT * FROM app_camera_usage ORDER BY lastAccessTime DESC")
    fun getAllUsage(): Flow<List<AppCameraUsage>>

    @Query("SELECT COUNT(DISTINCT packageName) FROM app_camera_usage WHERE date >= :todayMidnight")
    fun countTodayApps(todayMidnight: Long): Flow<Int>

    @Query("SELECT * FROM app_camera_usage WHERE packageName = :pkg ORDER BY lastAccessTime DESC")
    fun getUsageForPackage(pkg: String): Flow<List<AppCameraUsage>>

    @Query("DELETE FROM app_camera_usage WHERE date < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM app_camera_usage WHERE packageName = :pkg AND date = :date LIMIT 1")
    suspend fun findExisting(pkg: String, date: Long): AppCameraUsage?

    @Update
    suspend fun update(usage: AppCameraUsage)
}
