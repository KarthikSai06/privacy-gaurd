package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keystroke_profiles")
data class KeystrokeProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val avgDwellTime: Float,               // ms between key down and key up
    val avgFlightTime: Float,              // ms between consecutive key presses
    val typingSpeed: Float,                // chars per minute
    val sessionDuration: Long,             // ms
    val isOwner: Boolean = true,           // true = owner profile, false = intruder
    val timestamp: Long = System.currentTimeMillis()
)
