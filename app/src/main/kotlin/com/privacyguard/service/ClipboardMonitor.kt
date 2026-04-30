package com.privacyguard.service

import android.content.ClipboardManager
import android.content.Context
import com.privacyguard.utils.NotificationHelper

/**
 * Monitors clipboard changes and logs suspicious patterns.
 * Register via ClipboardManager.addPrimaryClipChangedListener in MainActivity or Application.
 */
class ClipboardMonitor(private val context: Context) {

    private var lastChangeTime = 0L
    private var changeCount = 0
    private val THRESHOLD_COUNT = 5
    private val THRESHOLD_WINDOW_MS = 10_000L // 10 seconds

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val now = System.currentTimeMillis()
        if (now - lastChangeTime < THRESHOLD_WINDOW_MS) {
            changeCount++
        } else {
            changeCount = 1
        }
        lastChangeTime = now

        if (changeCount >= THRESHOLD_COUNT) {
            NotificationHelper.notifySuspiciousActivity(
                context,
                "📋 Clipboard Alert",
                "Clipboard was accessed $changeCount times in ${THRESHOLD_WINDOW_MS / 1000}s. An app may be reading your clipboard."
            )
            changeCount = 0
        }
    }

    fun start() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener(listener)
    }

    fun stop() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.removePrimaryClipChangedListener(listener)
    }
}
