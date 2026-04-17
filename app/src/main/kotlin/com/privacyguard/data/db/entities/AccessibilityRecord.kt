package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accessibility_records")
data class AccessibilityRecord(
    @PrimaryKey val packageName: String,
    val appName: String,
    val serviceClass: String,
    val isSuspicious: Boolean,
    val firstDetectedAt: Long,  // epoch millis
    val notificationSent: Boolean = false
)
