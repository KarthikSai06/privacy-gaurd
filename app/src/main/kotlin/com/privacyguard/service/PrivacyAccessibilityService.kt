package com.privacyguard.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Enhanced AccessibilityService that detects potential keylogger behaviour.
 * Monitors TYPE_WINDOW_STATE_CHANGED for app transitions and
 * TYPE_VIEW_TEXT_CHANGED for rapid text interception (a common keylogger pattern).
 * Uses debouncing to prevent battery drain.
 */
class PrivacyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "PG_Accessibility"
        private const val TEXT_CHANGE_THRESHOLD = 10    // events per window
        private const val DEBOUNCE_WINDOW_MS = 60_000L  // 60 seconds
    }

    // packageName -> list of event timestamps within the debounce window
    private val textChangeCounters = HashMap<String, MutableList<Long>>()
    private val dlpAnalyzer = com.privacyguard.service.ScreenContentAnalyzer()
    private var lastDlpAlertTime = 0L

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val evt = event ?: return
        val pkg = evt.packageName?.toString() ?: return

        // Ignore our own package
        if (pkg == packageName) return

        when (evt.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Window state changes used for foreground app transitions
                // Data is handled by TriggerPairRepository via UsageStatsManager
            }

            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val now = System.currentTimeMillis()
                
                // --- 1. Keylogger Detection ---
                val timestamps = textChangeCounters.getOrPut(pkg) { mutableListOf() }
                timestamps.removeAll { now - it > DEBOUNCE_WINDOW_MS }
                timestamps.add(now)

                if (timestamps.size >= TEXT_CHANGE_THRESHOLD) {
                    Log.w(TAG, "Potential keylogger detected: $pkg (${timestamps.size} text changes in 60s)")
                    timestamps.clear()
                    com.privacyguard.utils.NotificationHelper.notifyKeylogger(applicationContext, pkg)
                }

                // --- 2. Screen Content DLP ---
                if (now - lastDlpAlertTime > DEBOUNCE_WINDOW_MS) {
                    val textOnScreen = evt.text.joinToString(" ")
                    if (textOnScreen.isNotBlank()) {
                        val matches = dlpAnalyzer.analyze(textOnScreen)
                        if (matches.isNotEmpty()) {
                            val typesFound = matches.map { it.type }.distinct().joinToString(", ")
                            Log.w(TAG, "Sensitive data exposed: $typesFound")
                            com.privacyguard.utils.NotificationHelper.notifyDlpAlert(applicationContext, pkg, typesFound)
                            lastDlpAlertTime = now
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() { /* no-op */ }
}
