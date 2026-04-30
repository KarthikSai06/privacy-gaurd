package com.privacyguard.data.db.dao

import androidx.room.*
import com.privacyguard.data.db.entities.KeystrokeProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface KeystrokeProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: KeystrokeProfile)

    @Query("SELECT * FROM keystroke_profiles WHERE isOwner = 1 ORDER BY timestamp DESC")
    fun getOwnerProfiles(): Flow<List<KeystrokeProfile>>

    @Query("SELECT * FROM keystroke_profiles WHERE isOwner = 0 ORDER BY timestamp DESC")
    fun getIntruderProfiles(): Flow<List<KeystrokeProfile>>

    @Query("SELECT AVG(avgDwellTime) FROM keystroke_profiles WHERE isOwner = 1")
    suspend fun getAvgOwnerDwellTime(): Float?

    @Query("SELECT AVG(avgFlightTime) FROM keystroke_profiles WHERE isOwner = 1")
    suspend fun getAvgOwnerFlightTime(): Float?

    @Query("SELECT AVG(typingSpeed) FROM keystroke_profiles WHERE isOwner = 1")
    suspend fun getAvgOwnerTypingSpeed(): Float?

    @Query("SELECT COUNT(*) FROM keystroke_profiles WHERE isOwner = 1")
    suspend fun getOwnerProfileCount(): Int

    @Query("DELETE FROM keystroke_profiles")
    suspend fun deleteAll()
}
