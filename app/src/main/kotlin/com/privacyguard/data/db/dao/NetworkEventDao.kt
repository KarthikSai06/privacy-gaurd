package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.NetworkEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: NetworkEvent): Long

    @Query("SELECT * FROM network_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NetworkEvent>>

    @Query("SELECT * FROM network_events WHERE isTracker = 1 ORDER BY timestamp DESC")
    fun getTrackerEvents(): Flow<List<NetworkEvent>>

    @Query("SELECT COUNT(*) FROM network_events WHERE isTracker = 1 AND date >= :todayMidnight")
    fun countTodayTrackers(todayMidnight: Long): Flow<Int>

    @Query("SELECT * FROM network_events WHERE packageName = :pkg ORDER BY timestamp DESC")
    fun getForPackage(pkg: String): Flow<List<NetworkEvent>>

    @Query("SELECT * FROM network_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSince(since: Long): Flow<List<NetworkEvent>>

    @Query("DELETE FROM network_events WHERE date < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM network_events")
    suspend fun deleteAll()
}
