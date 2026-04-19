package com.privacyguard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.privacyguard.data.repository.AccessibilityRepository
import com.privacyguard.data.repository.CameraUsageRepository
import com.privacyguard.data.repository.LocationUsageRepository
import com.privacyguard.data.repository.MicUsageRepository
import com.privacyguard.data.repository.NightActivityRepository
import com.privacyguard.data.repository.TriggerPairRepository
import com.privacyguard.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class PrivacyScanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val micUsageRepository: MicUsageRepository,
    private val cameraUsageRepository: CameraUsageRepository,
    private val locationUsageRepository: LocationUsageRepository,
    private val accessibilityRepository: AccessibilityRepository,
    private val nightActivityRepository: NightActivityRepository,
    private val triggerPairRepository: TriggerPairRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            micUsageRepository.scanAndStore()
            cameraUsageRepository.scanAndStore()
            locationUsageRepository.scanAndStore()
            accessibilityRepository.scanAndStore()
            nightActivityRepository.scanAndStore()
            triggerPairRepository.scanAndStore()
            
            // Basic anomaly alert check (example threshold)
            val recentNightActivity = nightActivityRepository.getFrom(System.currentTimeMillis() - 24 * 60 * 60 * 1000).firstOrNull()
            if (!recentNightActivity.isNullOrEmpty()) {
                NotificationHelper.notifySuspiciousActivity(
                    context,
                    "Privacy Alert",
                    "Detected ${recentNightActivity.size} background events last night."
                )
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "privacy_scan_periodic"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<PrivacyScanWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
