
package com.privacyguard.data.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import com.privacyguard.data.db.dao.AccessibilityDao
import com.privacyguard.data.db.entities.AccessibilityRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accessibilityDao: AccessibilityDao
) {
    companion object {
        const val CHANNEL_ID = "privacy_guard_alerts"

        /** Whitelist of known-safe accessibility service packages */
        val SAFE_PACKAGES = setOf(
            "com.google.android.googlequicksearchbox",
            "com.google.android.tts",
            "com.google.android.accessibility.talkback",
            "com.google.android.marvin.talkback",
            "com.google.android.apps.accessibility.voiceaccess",
            "com.samsung.accessibility",
            "com.samsung.android.app.talkback",
            "com.samsung.android.bixby.agent",
            "com.samsung.android.bixby.voice",
            "com.android.talkback",
            "com.android.settings",
            "com.android.systemui",
            "com.sec.android.app.launcher",
            "com.google.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.swiftkey.swiftkeyapp",
            "com.gboard",
            "com.google.android.gms"
        )
    }

    fun getAll(): Flow<List<AccessibilityRecord>> = accessibilityDao.getAll()
    fun getSuspicious(): Flow<List<AccessibilityRecord>> = accessibilityDao.getSuspicious()
    fun countSuspicious(): Flow<Int> = accessibilityDao.countSuspicious()

    suspend fun scanAndStore() = withContext(Dispatchers.IO) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val pm = context.packageManager

        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        val activePackages = mutableListOf<String>()

        for (serviceInfo in enabledServices) {
            val pkg = serviceInfo.resolveInfo.serviceInfo.packageName
            val svcClass = serviceInfo.resolveInfo.serviceInfo.name
            activePackages.add(pkg)

            val isSafe = SAFE_PACKAGES.any { pkg.startsWith(it) }
            val isSuspicious = !isSafe

            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (_: PackageManager.NameNotFoundException) { pkg }

            val existing = accessibilityDao.findByPackage(pkg)
            if (existing == null) {
                val record = AccessibilityRecord(
                    packageName = pkg,
                    appName = appName,
                    serviceClass = svcClass,
                    isSuspicious = isSuspicious,
                    firstDetectedAt = System.currentTimeMillis(),
                    notificationSent = false
                )
                accessibilityDao.insertOrReplace(record)
                if (isSuspicious) sendSuspiciousNotification(appName, pkg)
            } else if (isSuspicious && !existing.notificationSent) {
                sendSuspiciousNotification(appName, pkg)
                accessibilityDao.markNotificationSent(pkg)
            }
        }

        // Remove services no longer active
        if (activePackages.isNotEmpty()) {
            accessibilityDao.removeStale(activePackages)
        }
    }

    private fun sendSuspiciousNotification(appName: String, pkg: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "Privacy Alerts", NotificationManager.IMPORTANCE_HIGH
        )
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Suspicious Accessibility App")
            .setContentText("$appName ($pkg) has Accessibility access")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$appName may be acting as a keylogger. Tap to review Accessibility settings.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(pkg.hashCode(), notification)
    }
}
