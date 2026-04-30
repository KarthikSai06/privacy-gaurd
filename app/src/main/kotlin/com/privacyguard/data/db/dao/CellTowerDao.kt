package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.CellTowerLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CellTowerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CellTowerLog)

    @Query("SELECT * FROM cell_tower_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CellTowerLog>>

    @Query("SELECT * FROM cell_tower_logs WHERE isAnomaly = 1 ORDER BY timestamp DESC")
    fun getAnomalies(): Flow<List<CellTowerLog>>

    @Query("SELECT * FROM cell_tower_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<CellTowerLog>>

    @Query("SELECT DISTINCT cellId FROM cell_tower_logs WHERE isAnomaly = 0")
    suspend fun getKnownCellIds(): List<Int>

    @Query("DELETE FROM cell_tower_logs")
    suspend fun deleteAll()
}
