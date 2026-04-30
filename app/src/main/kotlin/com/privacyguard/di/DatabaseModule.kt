package com.privacyguard.di

import android.content.Context
import androidx.room.Room
import com.privacyguard.data.db.AppDatabase
import com.privacyguard.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMicUsageDao(db: AppDatabase): MicUsageDao = db.micUsageDao()
    @Provides fun provideCameraUsageDao(db: AppDatabase): CameraUsageDao = db.cameraUsageDao()
    @Provides fun provideLocationUsageDao(db: AppDatabase): LocationUsageDao = db.locationUsageDao()
    @Provides fun provideAccessibilityDao(db: AppDatabase): AccessibilityDao = db.accessibilityDao()
    @Provides fun provideNightActivityDao(db: AppDatabase): NightActivityDao = db.nightActivityDao()
    @Provides fun provideTriggerPairDao(db: AppDatabase): TriggerPairDao = db.triggerPairDao()
    @Provides fun providePrivacyEventDao(db: AppDatabase): PrivacyEventDao = db.privacyEventDao()
    @Provides fun provideNetworkEventDao(db: AppDatabase): NetworkEventDao = db.networkEventDao()
    @Provides fun provideDailyScoreDao(db: AppDatabase): DailyScoreDao = db.dailyScoreDao()
    @Provides fun provideCellTowerDao(db: AppDatabase): CellTowerDao = db.cellTowerDao()
    @Provides fun provideKeystrokeProfileDao(db: AppDatabase): KeystrokeProfileDao = db.keystrokeProfileDao()
    @Provides fun providePhishingAlertDao(db: AppDatabase): PhishingAlertDao = db.phishingAlertDao()
}
