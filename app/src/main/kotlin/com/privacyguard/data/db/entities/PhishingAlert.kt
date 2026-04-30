package com.privacyguard.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phishing_alerts")
data class PhishingAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,                    // SMS, NOTIFICATION, CLIPBOARD
    val senderOrApp: String,
    val content: String,
    val detectedUrl: String = "",
    val riskScore: Float,                  // 0.0 - 1.0
    val isPhishing: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
