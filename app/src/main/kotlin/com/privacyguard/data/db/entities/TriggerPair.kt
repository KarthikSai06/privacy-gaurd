package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_pairs")
data class TriggerPair(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appA: String,           // package name
    val appAName: String,
    val appB: String,           // package name
    val appBName: String,
    val firstSeen: Long,        // epoch millis
    val lastSeen: Long,         // epoch millis
    val count: Int = 1          // how many times this pair was observed
)
