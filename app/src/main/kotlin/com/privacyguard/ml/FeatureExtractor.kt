package com.privacyguard.ml

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.TrafficStats
import com.privacyguard.data.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class AppFeatures(
    val packageName: String,
    val appName: String,
    val micCount: Int,
    val cameraCount: Int,
    val locationCount: Int,
    val nightCount: Int,
    val isKeylogger: Boolean,
    val triggerCount: Int,
    val anomalyScore: Float
)

@Singleton
class FeatureExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val nightRepo: NightActivityRepository,
    private val triggerRepo: TriggerPairRepository
) {
    suspend fun extractAll(detector: AnomalyDetector): List<AppFeatures> {
        val pm = context.packageManager
        
        val micList      = micRepo.getAllUsage().firstOrNull() ?: emptyList()
        val camList      = cameraRepo.getAllUsage().firstOrNull() ?: emptyList()
        val locList      = locationRepo.getAllUsage().firstOrNull() ?: emptyList()
        val nightList    = nightRepo.getAll().firstOrNull() ?: emptyList()
        val suspicious   = accessibilityRepo.getSuspicious().firstOrNull() ?: emptyList()
        val triggerList  = triggerRepo.getAll().firstOrNull() ?: emptyList()

        // Aggregate per package based on active monitoring
        val pkgSet = (micList.map { it.packageName } +
                      camList.map { it.packageName } +
                      locList.map { it.packageName } +
                      suspicious.map { it.packageName }).toSet()

        return pkgSet.mapNotNull { pkg ->
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS or PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES)
            } catch (e: PackageManager.NameNotFoundException) {
                // App uninstalled
            }

            if (packageInfo == null) return@mapNotNull null

            // 1. Static features from PackageManager
            val requestedPerms = packageInfo.requestedPermissions?.toList() ?: emptyList()
            val permAudio = if (requestedPerms.contains("android.permission.RECORD_AUDIO")) 1f else 0f
            val permCamera = if (requestedPerms.contains("android.permission.CAMERA")) 1f else 0f
            val permLocation = if (requestedPerms.contains("android.permission.ACCESS_FINE_LOCATION")) 1f else 0f
            val totalPerms = requestedPerms.size.toFloat()
            val receiversCount = packageInfo.receivers?.size?.toFloat() ?: 0f
            val servicesCount = packageInfo.services?.size?.toFloat() ?: 0f

            // 2. Dynamic features from DAOs
            val mic    = micList.count { it.packageName == pkg }
            val cam    = camList.count { it.packageName == pkg }
            val loc    = locList.count { it.packageName == pkg }
            val night  = nightList.count { it.packageName == pkg }
            val kl     = suspicious.any { it.packageName == pkg }
            val trig   = triggerList.count { it.appA == pkg || it.appB == pkg }
            
            // Network bytes via TrafficStats
            val uid = packageInfo.applicationInfo.uid
            val networkBytes = TrafficStats.getUidTxBytes(uid).let { if (it > 0) it.toFloat() else 0f }

            val appName = pm.getApplicationLabel(packageInfo.applicationInfo).toString()

            // 13 feature array exactly matching TFLite model expectations
            val features = floatArrayOf(
                permAudio,
                permCamera,
                permLocation,
                totalPerms,
                receiversCount,
                servicesCount,
                mic.toFloat(),
                cam.toFloat(),
                loc.toFloat(),
                networkBytes,
                night.toFloat(),
                trig.toFloat(),
                if (kl) 1f else 0f
            )

            AppFeatures(
                packageName  = pkg,
                appName      = appName,
                micCount     = mic,
                cameraCount  = cam,
                locationCount = loc,
                nightCount   = night,
                isKeylogger  = kl,
                triggerCount = trig,
                anomalyScore = detector.score(features)
            )
        }.sortedByDescending { it.anomalyScore }
    }
}
