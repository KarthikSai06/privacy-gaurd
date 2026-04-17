package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.AccessibilityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessibilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(record: AccessibilityRecord)

    @Query("SELECT * FROM accessibility_records ORDER BY isSuspicious DESC, appName ASC")
    fun getAll(): Flow<List<AccessibilityRecord>>

    @Query("SELECT * FROM accessibility_records WHERE isSuspicious = 1")
    fun getSuspicious(): Flow<List<AccessibilityRecord>>

    @Query("SELECT COUNT(*) FROM accessibility_records WHERE isSuspicious = 1")
    fun countSuspicious(): Flow<Int>

    @Query("SELECT * FROM accessibility_records WHERE packageName = :pkg LIMIT 1")
    suspend fun findByPackage(pkg: String): AccessibilityRecord?

    @Query("UPDATE accessibility_records SET notificationSent = 1 WHERE packageName = :pkg")
    suspend fun markNotificationSent(pkg: String)

    @Query("DELETE FROM accessibility_records WHERE packageName NOT IN (:activePackages)")
    suspend fun removeStale(activePackages: List<String>)
}
