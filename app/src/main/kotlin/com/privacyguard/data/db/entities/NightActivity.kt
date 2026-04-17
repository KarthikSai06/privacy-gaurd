package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "night_activities")
data class NightActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long,    // epoch millis when the activity occurred
    val date: Long          // midnight epoch of the day (for grouping/filtering)
)
