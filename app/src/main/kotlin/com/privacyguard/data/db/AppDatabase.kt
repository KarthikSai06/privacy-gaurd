package com.privacyguard.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.privacyguard.data.db.dao.AccessibilityDao
import com.privacyguard.data.db.dao.CameraUsageDao
import com.privacyguard.data.db.dao.LocationUsageDao
import com.privacyguard.data.db.dao.MicUsageDao
import com.privacyguard.data.db.dao.NightActivityDao
import com.privacyguard.data.db.dao.TriggerPairDao
import com.privacyguard.data.db.entities.AccessibilityRecord
import com.privacyguard.data.db.entities.AppCameraUsage
import com.privacyguard.data.db.entities.AppLocationUsage
import com.privacyguard.data.db.entities.AppMicUsage
import com.privacyguard.data.db.entities.NightActivity
import com.privacyguard.data.db.entities.TriggerPair

@Database(
    entities = [
        AppMicUsage::class,
        AppCameraUsage::class,
        AppLocationUsage::class,
        AccessibilityRecord::class,
        NightActivity::class,
        TriggerPair::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun micUsageDao(): MicUsageDao
    abstract fun cameraUsageDao(): CameraUsageDao
    abstract fun locationUsageDao(): LocationUsageDao
    abstract fun accessibilityDao(): AccessibilityDao
    abstract fun nightActivityDao(): NightActivityDao
    abstract fun triggerPairDao(): TriggerPairDao

    companion object {
        const val DATABASE_NAME = "privacy_guard_db"
    }
}
