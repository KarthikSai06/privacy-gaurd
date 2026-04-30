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
                // Track text change events per package for keylogger detection
                val now = System.currentTimeMillis()
                val timestamps = textChangeCounters.getOrPut(pkg) { mutableListOf() }

                // Remove events outside the debounce window
                timestamps.removeAll { now - it > DEBOUNCE_WINDOW_MS }

                // Add current event
                timestamps.add(now)

                // Check threshold
                if (timestamps.size >= TEXT_CHANGE_THRESHOLD) {
                    Log.w(TAG, "Potential keylogger detected: $pkg (${timestamps.size} text changes in 60s)")
                    // Clear counter to avoid spamming
                    timestamps.clear()

                    // Notify user
                    com.privacyguard.utils.NotificationHelper.notifyKeylogger(
                        applicationContext, pkg
                    )
                }
            }
        }
    }

    override fun onInterrupt() { /* no-op */ }
}
