package com.privacyguard.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.privacyguard.data.db.dao.*
import com.privacyguard.data.db.entities.*

@Database(
    entities = [
        AppMicUsage::class,
        AppCameraUsage::class,
        AppLocationUsage::class,
        AccessibilityRecord::class,
        NightActivity::class,
        TriggerPair::class,
        PrivacyEvent::class,
        NetworkEvent::class,
        DailyScore::class,
        CellTowerLog::class,
        KeystrokeProfile::class,
        PhishingAlert::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun micUsageDao(): MicUsageDao
    abstract fun cameraUsageDao(): CameraUsageDao
    abstract fun locationUsageDao(): LocationUsageDao
    abstract fun accessibilityDao(): AccessibilityDao
    abstract fun nightActivityDao(): NightActivityDao
    abstract fun triggerPairDao(): TriggerPairDao
    abstract fun privacyEventDao(): PrivacyEventDao
    abstract fun networkEventDao(): NetworkEventDao
    abstract fun dailyScoreDao(): DailyScoreDao
    abstract fun cellTowerDao(): CellTowerDao
    abstract fun keystrokeProfileDao(): KeystrokeProfileDao
    abstract fun phishingAlertDao(): PhishingAlertDao

    companion object {
        const val DATABASE_NAME = "privacy_guard_db"
    }
}
