package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.PrivacyEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: PrivacyEvent): Long

    @Query("SELECT * FROM privacy_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PrivacyEvent>>

    @Query("SELECT * FROM privacy_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSince(since: Long): Flow<List<PrivacyEvent>>

    @Query("SELECT * FROM privacy_events WHERE eventType = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<PrivacyEvent>>

    @Query("SELECT * FROM privacy_events WHERE eventType = :type AND timestamp >= :since ORDER BY timestamp DESC")
    fun getByTypeAndSince(type: String, since: Long): Flow<List<PrivacyEvent>>

    @Query("SELECT * FROM privacy_events WHERE packageName = :pkg ORDER BY timestamp DESC")
    fun getForPackage(pkg: String): Flow<List<PrivacyEvent>>

    @Query("DELETE FROM privacy_events WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM privacy_events")
    suspend fun deleteAll()
}
