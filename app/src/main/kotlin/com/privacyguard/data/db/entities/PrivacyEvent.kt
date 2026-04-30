package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_events")
data class PrivacyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val eventType: String,  // MIC, CAMERA, LOCATION, NIGHT, KEYLOGGER, TRIGGER, CLIPBOARD, NETWORK
    val timestamp: Long,    // epoch millis
    val details: String = ""
)
