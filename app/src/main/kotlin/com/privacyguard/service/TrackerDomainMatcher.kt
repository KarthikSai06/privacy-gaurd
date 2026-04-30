package com.privacyguard.service

import android.content.Context

/**
 * Matches domain names against a bundled list of known advertising/tracking domains.
 * Supports subdomain matching (e.g., "ads.example.com" matches "example.com").
 */
class TrackerDomainMatcher(context: Context) {

    private val trackerDomains: Set<String> by lazy {
        try {
            context.assets.open("tracker_domains.txt").bufferedReader().useLines { lines ->
                lines.filter { it.isNotBlank() && !it.startsWith("#") }
                    .map { it.trim().lowercase() }
                    .toSet()
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    /**
     * Returns true if the given domain (or any of its parent domains) is in the tracker list.
     */
    fun isTracker(domain: String): Boolean {
        val normalized = domain.trim().lowercase().removeSuffix(".")
        if (normalized.isBlank()) return false

        // Check exact match
        if (normalized in trackerDomains) return true

        // Check parent domains (subdomain matching)
        val parts = normalized.split(".")
        for (i in 1 until parts.size - 1) {
            val parent = parts.subList(i, parts.size).joinToString(".")
            if (parent in trackerDomains) return true
        }

        return false
    }
}
