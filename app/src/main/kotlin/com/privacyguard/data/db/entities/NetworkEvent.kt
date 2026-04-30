package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_events")
data class NetworkEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val domain: String,
    val isTracker: Boolean,
    val timestamp: Long,    // epoch millis
    val date: Long          // midnight epoch of the day (for grouping)
)
