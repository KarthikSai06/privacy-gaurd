package com.privacyguard.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.privacyguard.data.db.dao.TriggerPairDao
import com.privacyguard.data.db.entities.TriggerPair
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerPairRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val triggerPairDao: TriggerPairDao
) {
    companion object {
        const val TRIGGER_WINDOW_MS = 5_000L // 5 seconds
    }

    fun getAll(): Flow<List<TriggerPair>> = triggerPairDao.getAll()
    fun count(): Flow<Int> = triggerPairDao.count()

    /** Scans the last 24h of UsageEvents for co-activation patterns within 5 seconds. */
    suspend fun scanAndStore() = withContext(Dispatchers.IO) {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24L * 60 * 60 * 1000

        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        val foregroundEvents = mutableListOf<Pair<String, Long>>() // pkg to timestamp

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType != UsageEvents.Event.ACTIVITY_RESUMED) continue
            val pkg = event.packageName
            // Skip system apps
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) continue
            } catch (_: PackageManager.NameNotFoundException) { continue }

            foregroundEvents.add(pkg to event.timeStamp)
        }

        // Sort by time
        foregroundEvents.sortBy { it.second }

        // Detect pairs: if B comes within 5s after A
        for (i in 0 until foregroundEvents.size - 1) {
            val (pkgA, tsA) = foregroundEvents[i]
            val (pkgB, tsB) = foregroundEvents[i + 1]
            if (pkgA == pkgB) continue
            if (tsB - tsA <= TRIGGER_WINDOW_MS) {
                val appAName = getLabel(pm, pkgA)
                val appBName = getLabel(pm, pkgB)
                val now = System.currentTimeMillis()
                val existing = triggerPairDao.findPair(pkgA, pkgB)
                if (existing != null) {
                    triggerPairDao.incrementCount(pkgA, pkgB, now)
                } else {
                    triggerPairDao.insert(
                        TriggerPair(
                            appA = pkgA, appAName = appAName,
                            appB = pkgB, appBName = appBName,
                            firstSeen = tsA, lastSeen = now
                        )
                    )
                }
            }
        }
    }

    private fun getLabel(pm: PackageManager, pkg: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) { pkg }
}
