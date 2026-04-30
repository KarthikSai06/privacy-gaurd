package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_scores")
data class DailyScore(
    @PrimaryKey val date: String,          // yyyy-MM-dd
    val overallScore: Int,                 // 0-100
    val appsScanned: Int,
    val threatsDetected: Int,
    val permissionsRevoked: Int = 0,
    val trackersDomain: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
