package com.privacyguard.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

/**
 * Minimal AccessibilityService stub.
 * Real-time foreground detection is achieved via UsageStatsManager;
 * this service is declared so the user CAN grant it, enabling the
 * foreground-detection fallback path on devices that restrict UsageStats.
 */
class PrivacyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Window state changes can be used to detect foreground app transitions
        // Data is handled by TriggerPairRepository via UsageStatsManager
    }

    override fun onInterrupt() { /* no-op */ }
}
