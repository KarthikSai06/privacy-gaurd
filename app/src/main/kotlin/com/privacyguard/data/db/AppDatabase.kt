package com.privacyguard.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.privacyguard.data.db.dao.AccessibilityDao
import com.privacyguard.data.db.dao.MicUsageDao
import com.privacyguard.data.db.dao.NightActivityDao
import com.privacyguard.data.db.dao.TriggerPairDao
import com.privacyguard.data.db.entities.AccessibilityRecord
import com.privacyguard.data.db.entities.AppMicUsage
import com.privacyguard.data.db.entities.NightActivity
import com.privacyguard.data.db.entities.TriggerPair

@Database(
    entities = [
        AppMicUsage::class,
        AccessibilityRecord::class,
        NightActivity::class,
        TriggerPair::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun micUsageDao(): MicUsageDao
    abstract fun accessibilityDao(): AccessibilityDao
    abstract fun nightActivityDao(): NightActivityDao
    abstract fun triggerPairDao(): TriggerPairDao

    companion object {
        const val DATABASE_NAME = "privacy_guard_db"
    }
}
