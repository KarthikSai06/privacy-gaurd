package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.PhishingAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhishingAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: PhishingAlert)

    @Query("SELECT * FROM phishing_alerts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PhishingAlert>>

    @Query("SELECT * FROM phishing_alerts WHERE isPhishing = 1 ORDER BY timestamp DESC")
    fun getPhishingOnly(): Flow<List<PhishingAlert>>

    @Query("SELECT COUNT(*) FROM phishing_alerts WHERE isPhishing = 1")
    suspend fun getPhishingCount(): Int

    @Query("DELETE FROM phishing_alerts")
    suspend fun deleteAll()
}
