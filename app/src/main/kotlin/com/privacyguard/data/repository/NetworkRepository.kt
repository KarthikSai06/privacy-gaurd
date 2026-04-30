package com.privacyguard.data.repository

import com.privacyguard.data.db.dao.NetworkEventDao
import com.privacyguard.data.db.entities.NetworkEvent
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    private val networkEventDao: NetworkEventDao
) {
    fun getAll(): Flow<List<NetworkEvent>> = networkEventDao.getAll()

    fun getTrackerEvents(): Flow<List<NetworkEvent>> = networkEventDao.getTrackerEvents()

    fun countTodayTrackers(): Flow<Int> = networkEventDao.countTodayTrackers(todayMidnight())

    fun getForPackage(pkg: String): Flow<List<NetworkEvent>> = networkEventDao.getForPackage(pkg)

    fun getSince(since: Long): Flow<List<NetworkEvent>> = networkEventDao.getSince(since)

    suspend fun insert(event: NetworkEvent) {
        networkEventDao.insert(event)
    }

    suspend fun deleteOlderThan(before: Long) {
        networkEventDao.deleteOlderThan(before)
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
