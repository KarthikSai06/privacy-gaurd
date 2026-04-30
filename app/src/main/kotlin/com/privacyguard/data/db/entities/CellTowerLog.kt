package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_tower_logs")
data class CellTowerLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cellId: Int,
    val lac: Int,                          // Location Area Code
    val signalStrength: Int,               // dBm
    val networkType: String,               // LTE, GSM, WCDMA
    val isAnomaly: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
