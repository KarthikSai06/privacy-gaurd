package com.privacyguard.data.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.privacyguard.data.db.dao.NightActivityDao
import com.privacyguard.data.db.entities.NightActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NightActivityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nightActivityDao: NightActivityDao
) {
    fun getAll(): Flow<List<NightActivity>> = nightActivityDao.getAll()

    fun getFrom(fromDate: Long): Flow<List<NightActivity>> = nightActivityDao.getFrom(fromDate)

    fun countThisWeek(): Flow<Int> = nightActivityDao.countThisWeek(weekAgo())

    /** Scans UsageStatsManager for the past 7 days for night-time (1AM–5AM) usage. */
    suspend fun scanAndStore(nightStartHour: Int = 1, nightEndHour: Int = 5) =
        withContext(Dispatchers.IO) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val pm = context.packageManager

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 7L * 24 * 60 * 60 * 1000

            val stats: Map<String, UsageStats> = usm.queryAndAggregateUsageStats(startTime, endTime)

            // We need per-event data to get accurate timestamps; use queryEvents
            val events = usm.queryEvents(startTime, endTime)
            val event = android.app.usage.UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType != android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) continue

                val pkg = event.packageName
                val ts = event.timeStamp

                // Check if it falls in night window
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour < nightStartHour || hour >= nightEndHour) continue

                // Skip system apps
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (isSystem) continue
                } catch (_: PackageManager.NameNotFoundException) { continue }

                val dayMidnight = cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val existing = nightActivityDao.findExisting(pkg, dayMidnight)
                if (existing == null) {
                    val appName = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                    } catch (_: Exception) { pkg }

                    nightActivityDao.insert(
                        NightActivity(
                            packageName = pkg,
                            appName = appName,
                            timestamp = ts,
                            date = dayMidnight
                        )
                    )
                }
            }

            // Prune older than 30 days
            nightActivityDao.deleteOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
        }

    private fun weekAgo() = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
}
