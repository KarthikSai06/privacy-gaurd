package com.privacyguard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.privacyguard.data.repository.AccessibilityRepository
import com.privacyguard.data.repository.MicUsageRepository
import com.privacyguard.data.repository.NightActivityRepository
import com.privacyguard.data.repository.TriggerPairRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PrivacyScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val micUsageRepository: MicUsageRepository,
    private val accessibilityRepository: AccessibilityRepository,
    private val nightActivityRepository: NightActivityRepository,
    private val triggerPairRepository: TriggerPairRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            micUsageRepository.scanAndStore()
            accessibilityRepository.scanAndStore()
            nightActivityRepository.scanAndStore()
            triggerPairRepository.scanAndStore()
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
