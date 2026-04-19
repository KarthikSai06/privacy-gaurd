package com.privacyguard.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.privacyguard.data.db.dao.LocationUsageDao
import com.privacyguard.data.db.entities.AppLocationUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationUsageDao: LocationUsageDao
) {

    fun getAllUsage(): Flow<List<AppLocationUsage>> = locationUsageDao.getAllUsage()

    fun getUsageSince(since: Long): Flow<List<AppLocationUsage>> = locationUsageDao.getUsageSince(since)

    fun countTodayApps(): Flow<Int> = locationUsageDao.countTodayApps(todayMidnight())

    /** Queries AppOpsManager for FINE_LOCATION and persists results into Room. */
    suspend fun scanAndStore() = withContext(Dispatchers.IO) {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val pm = context.packageManager
        val today = todayMidnight()

        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in packages) {
            try {
                val pkg = appInfo.packageName
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_FINE_LOCATION, appInfo.uid, pkg
                    )
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_FINE_LOCATION, appInfo.uid, pkg
                    )
                }

                if (mode == AppOpsManager.MODE_ALLOWED) {
                    val lastAccess = System.currentTimeMillis() // Placeholder
                    val duration = 0L // Placeholder
                    
                    if (lastAccess > 0) {
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        val existing = locationUsageDao.findExisting(pkg, today)
                        if (existing != null) {
                            locationUsageDao.update(
                                existing.copy(
                                    lastAccessTime = maxOf(existing.lastAccessTime, lastAccess),
                                    durationMs = existing.durationMs + duration
                                )
                            )
                        } else {
                            locationUsageDao.insert(
                                AppLocationUsage(
                                    packageName = pkg,
                                    appName = appName,
                                    lastAccessTime = lastAccess,
                                    durationMs = duration,
                                    date = today
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // Prune records older than 30 days
        locationUsageDao.deleteOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    }

    private fun todayMidnight(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
