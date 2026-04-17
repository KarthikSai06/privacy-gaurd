package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_mic_usage")
data class AppMicUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val lastAccessTime: Long,   // epoch millis
    val durationMs: Long,       // duration in ms
    val date: Long              // midnight epoch of the day (for grouping)
)
