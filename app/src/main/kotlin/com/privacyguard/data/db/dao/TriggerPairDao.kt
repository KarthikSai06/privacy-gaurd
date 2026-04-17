package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.TriggerPair
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerPairDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pair: TriggerPair): Long

    @Query("SELECT * FROM trigger_pairs ORDER BY count DESC, lastSeen DESC")
    fun getAll(): Flow<List<TriggerPair>>

    @Query("SELECT COUNT(*) FROM trigger_pairs")
    fun count(): Flow<Int>

    @Query("""
        SELECT * FROM trigger_pairs 
        WHERE appA = :pkgA AND appB = :pkgB LIMIT 1
    """)
    suspend fun findPair(pkgA: String, pkgB: String): TriggerPair?

    @Query("""
        UPDATE trigger_pairs 
        SET count = count + 1, lastSeen = :lastSeen 
        WHERE appA = :pkgA AND appB = :pkgB
    """)
    suspend fun incrementCount(pkgA: String, pkgB: String, lastSeen: Long)

    @Query("DELETE FROM trigger_pairs")
    suspend fun deleteAll()
}
