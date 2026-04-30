package com.privacyguard.di

import android.content.Context
import androidx.room.Room
import com.privacyguard.data.db.AppDatabase
import com.privacyguard.data.db.dao.AccessibilityDao
import com.privacyguard.data.db.dao.CameraUsageDao
import com.privacyguard.data.db.dao.LocationUsageDao
import com.privacyguard.data.db.dao.MicUsageDao
import com.privacyguard.data.db.dao.NetworkEventDao
import com.privacyguard.data.db.dao.NightActivityDao
import com.privacyguard.data.db.dao.PrivacyEventDao
import com.privacyguard.data.db.dao.TriggerPairDao
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

    @Provides
    fun provideMicUsageDao(db: AppDatabase): MicUsageDao = db.micUsageDao()
    
    @Provides
    fun provideCameraUsageDao(db: AppDatabase): CameraUsageDao = db.cameraUsageDao()
    
    @Provides
    fun provideLocationUsageDao(db: AppDatabase): LocationUsageDao = db.locationUsageDao()

    @Provides
    fun provideAccessibilityDao(db: AppDatabase): AccessibilityDao = db.accessibilityDao()

    @Provides
    fun provideNightActivityDao(db: AppDatabase): NightActivityDao = db.nightActivityDao()

    @Provides
    fun provideTriggerPairDao(db: AppDatabase): TriggerPairDao = db.triggerPairDao()

    @Provides
    fun providePrivacyEventDao(db: AppDatabase): PrivacyEventDao = db.privacyEventDao()

    @Provides
    fun provideNetworkEventDao(db: AppDatabase): NetworkEventDao = db.networkEventDao()
}
