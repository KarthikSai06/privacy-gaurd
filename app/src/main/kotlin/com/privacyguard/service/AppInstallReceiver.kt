package com.privacyguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.util.Log
import com.privacyguard.ml.AnomalyDetector
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * App Install Scanner — Automatically scans newly installed apps
 * using the TFLite spyware detection model and sends smart notifications.
 */
@AndroidEntryPoint
class AppInstallReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationManager: SmartNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return // update, not new install

        val packageName = intent.data?.schemeSpecificPart ?: return
        Log.d("AppInstallReceiver", "New app installed: $packageName")

        try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES
            )

            val requestedPerms = packageInfo.requestedPermissions?.toList() ?: emptyList()

            // Build the 13-feature vector
            val features = floatArrayOf(
                if (requestedPerms.contains("android.permission.RECORD_AUDIO")) 1f else 0f,
                if (requestedPerms.contains("android.permission.CAMERA")) 1f else 0f,
                if (requestedPerms.contains("android.permission.ACCESS_FINE_LOCATION")) 1f else 0f,
                requestedPerms.size.toFloat(),
                (packageInfo.receivers?.size ?: 0).toFloat(),
                (packageInfo.services?.size ?: 0).toFloat(),
                0f, // mic usage (new app, no data yet)
                0f, // camera usage
                0f, // location usage
                TrafficStats.getUidTxBytes(packageInfo.applicationInfo.uid).let { if (it > 0) it.toFloat() else 0f },
                0f, // night activity
                0f, // co-triggers
                0f  // keylogger flag
            )

            val detector = AnomalyDetector(context)
            val score = detector.score(features)
            val riskPercent = (score * 100).toInt()
            val appName = pm.getApplicationLabel(packageInfo.applicationInfo).toString()

            notificationManager.notifyNewAppScanned(appName, riskPercent)
            detector.close()

        } catch (e: Exception) {
            Log.e("AppInstallReceiver", "Failed to scan $packageName", e)
        }
    }
}
