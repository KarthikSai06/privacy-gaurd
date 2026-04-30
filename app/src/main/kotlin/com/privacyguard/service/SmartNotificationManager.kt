package com.privacyguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.privacyguard.R
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Smart Notification Manager — Proactive privacy alerts with priority channels.
 */
@Singleton
class SmartNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_CRITICAL = "privacy_critical"
        const val CHANNEL_WARNING = "privacy_warning"
        const val CHANNEL_INFO = "privacy_info"
        private var notifId = 1000
    }

    init { createChannels() }

    private fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_CRITICAL, "Critical Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Urgent privacy threats requiring immediate action" })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_WARNING, "Warnings",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Privacy warnings and suspicious activity" })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_INFO, "Information",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Privacy tips and status updates" })
    }

    fun notifyCritical(title: String, message: String) {
        sendNotification(CHANNEL_CRITICAL, "🔴 $title", message, NotificationCompat.PRIORITY_HIGH)
    }

    fun notifyWarning(title: String, message: String) {
        sendNotification(CHANNEL_WARNING, "🟡 $title", message, NotificationCompat.PRIORITY_DEFAULT)
    }

    fun notifyInfo(title: String, message: String) {
        sendNotification(CHANNEL_INFO, "🟢 $title", message, NotificationCompat.PRIORITY_LOW)
    }

    fun notifyNewAppScanned(appName: String, riskScore: Int) {
        when {
            riskScore >= 70 -> notifyCritical(
                "Dangerous App Installed",
                "$appName scored $riskScore/100. This app shows spyware-like behavior!"
            )
            riskScore >= 40 -> notifyWarning(
                "Suspicious App Installed",
                "$appName scored $riskScore/100. Review its permissions."
            )
            else -> notifyInfo(
                "New App Scanned",
                "$appName scored $riskScore/100 — appears safe."
            )
        }
    }

    fun notifyExcessiveSensorUse(appName: String, sensor: String, count: Int, duration: String) {
        notifyWarning(
            "Excessive $sensor Usage",
            "$appName accessed $sensor $count times in the last $duration."
        )
    }

    fun notifyPhishingDetected(source: String, preview: String) {
        notifyCritical(
            "Phishing Detected",
            "Suspicious message from $source: \"${preview.take(80)}...\""
        )
    }

    fun notifyImsiCatcher(cellId: Int) {
        notifyCritical(
            "Fake Cell Tower Detected",
            "Unknown cell tower (ID: $cellId) detected. Your calls may be intercepted!"
        )
    }

    fun notifyIntruder(anomalyScore: Float) {
        notifyCritical(
            "Unauthorized User Detected",
            "Typing pattern doesn't match device owner (confidence: ${(anomalyScore * 100).toInt()}%)."
        )
    }

    fun notifySensitiveDataExposed(dataType: String, appName: String) {
        notifyWarning(
            "Sensitive Data on Screen",
            "$dataType detected while $appName is active. Be cautious of shoulder surfing."
        )
    }

    private fun sendNotification(channel: String, title: String, message: String, priority: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(notifId++, notification)
    }
}
