package com.privacyguard.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.privacyguard.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "privacy_alerts"
    private const val CHANNEL_NAME = "Privacy Alerts"

    // Unique notification IDs per category
    private const val ID_GENERIC       = 1001
    private const val ID_CAMERA_BASE   = 2000
    private const val ID_LOCATION_BASE = 3000
    private const val ID_NIGHT         = 4001
    private const val ID_KEYLOGGER     = 5001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "High priority privacy threat alerts"
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // ── Generic ────────────────────────────────────────────────────────────
    fun notifySuspiciousActivity(context: Context, title: String, message: String) {
        send(context, ID_GENERIC, title, message)
    }

    // ── Camera ─────────────────────────────────────────────────────────────
    fun notifyCameraAbuse(context: Context, appName: String) {
        send(
            context,
            ID_CAMERA_BASE + appName.hashCode() % 100,
            "📷 Camera Access Detected",
            "$appName accessed your camera unexpectedly."
        )
    }

    // ── Location ───────────────────────────────────────────────────────────
    fun notifyLocationAbuse(context: Context, appName: String) {
        send(
            context,
            ID_LOCATION_BASE + appName.hashCode() % 100,
            "📍 Location Access Detected",
            "$appName queried your GPS location."
        )
    }

    // ── Night anomaly ──────────────────────────────────────────────────────
    fun notifyNightAnomaly(context: Context, count: Int) {
        send(
            context,
            ID_NIGHT,
            "🌙 Night-time Activity Detected",
            "$count app(s) were active between midnight and 5 AM while you slept."
        )
    }

    // ── Keylogger ──────────────────────────────────────────────────────────
    fun notifyKeylogger(context: Context, appName: String) {
        send(
            context,
            ID_KEYLOGGER,
            "⚠️ Suspicious Accessibility App",
            "$appName may be acting as a keylogger. Review Accessibility settings."
        )
    }

    // ── Screen DLP ─────────────────────────────────────────────────────────
    fun notifyDlpAlert(context: Context, appName: String, dataType: String) {
        send(
            context,
            ID_KEYLOGGER + 1, // Unique enough
            "🛡️ Sensitive Data Exposed",
            "$dataType detected on screen while $appName is active. Beware of shoulder surfing!"
        )
    }

    // ── Internal sender ────────────────────────────────────────────────────
    private fun send(context: Context, notifId: Int, title: String, message: String) {
        createNotificationChannel(context)

        val pendingIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notify(notifId, notification)
            }
        }
    }
}
