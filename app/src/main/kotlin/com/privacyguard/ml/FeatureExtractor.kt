package com.privacyguard.ml

import com.privacyguard.data.db.entities.*
import com.privacyguard.data.repository.*
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
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val nightRepo: NightActivityRepository,
    private val triggerRepo: TriggerPairRepository
) {
    suspend fun extractAll(detector: AnomalyDetector): List<AppFeatures> {
        val micList      = micRepo.getAllUsage().firstOrNull() ?: emptyList()
        val camList      = cameraRepo.getAllUsage().firstOrNull() ?: emptyList()
        val locList      = locationRepo.getAllUsage().firstOrNull() ?: emptyList()
        val nightList    = nightRepo.getAll().firstOrNull() ?: emptyList()
        val suspicious   = accessibilityRepo.getSuspicious().firstOrNull() ?: emptyList()
        val triggerList  = triggerRepo.getAll().firstOrNull() ?: emptyList()

        // Aggregate per package
        val pkgSet = (micList.map { it.packageName } +
                      camList.map { it.packageName } +
                      locList.map { it.packageName } +
                      suspicious.map { it.packageName }).toSet()

        // Max values for normalization
        val maxMic   = micList.size.coerceAtLeast(1).toFloat()
        val maxCam   = camList.size.coerceAtLeast(1).toFloat()
        val maxLoc   = locList.size.coerceAtLeast(1).toFloat()
        val maxNight = nightList.size.coerceAtLeast(1).toFloat()
        val maxTrig  = triggerList.size.coerceAtLeast(1).toFloat()

        return pkgSet.map { pkg ->
            val mic    = micList.count { it.packageName == pkg }
            val cam    = camList.count { it.packageName == pkg }
            val loc    = locList.count { it.packageName == pkg }
            val night  = nightList.count { it.packageName == pkg }
            val kl     = suspicious.any { it.packageName == pkg }
            val trig   = triggerList.count { it.appA == pkg || it.appB == pkg }

            val appName = (micList.find { it.packageName == pkg }?.appName
                ?: camList.find { it.packageName == pkg }?.appName
                ?: locList.find { it.packageName == pkg }?.appName
                ?: suspicious.find { it.packageName == pkg }?.appName
                ?: pkg)

            val features = floatArrayOf(
                mic / maxMic,
                cam / maxCam,
                loc / maxLoc,
                night / maxNight,
                if (kl) 1f else 0f,
                trig / maxTrig
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
