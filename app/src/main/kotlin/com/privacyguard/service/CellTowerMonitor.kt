package com.privacyguard.service

import android.content.Context
import android.os.Build
import android.telephony.*
import android.util.Log
import com.privacyguard.data.db.dao.CellTowerDao
import com.privacyguard.data.db.entities.CellTowerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Cell Tower Monitor — IMSI Catcher / Fake Base Station Detection.
 *
 * Monitors cell tower changes and flags anomalies:
 * - Unknown cell IDs not seen before
 * - Sudden network type downgrades (4G → 2G)
 * - Abnormal signal strength jumps
 */
@Singleton
class CellTowerMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cellTowerDao: CellTowerDao,
    private val notificationManager: SmartNotificationManager
) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastNetworkType: String = ""
    private var lastSignalStrength: Int = 0
    private var isMonitoring = false

    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            cellInfo?.forEach { analyzeCellInfo(it) }
        }

        @Deprecated("Deprecated in Java")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            signalStrength?.let {
                val dbm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.cellSignalStrengths.firstOrNull()?.dbm ?: -999
                } else {
                    it.gsmSignalStrength * 2 - 113
                }
                checkSignalAnomaly(dbm)
            }
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        try {
            telephonyManager.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_CELL_INFO or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
            )
            isMonitoring = true
            Log.d("CellTowerMonitor", "Monitoring started")
        } catch (e: SecurityException) {
            Log.e("CellTowerMonitor", "Missing READ_PHONE_STATE permission", e)
        }
    }

    fun stopMonitoring() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        isMonitoring = false
    }

    private fun analyzeCellInfo(info: CellInfo) {
        val cellId: Int
        val lac: Int
        val networkType: String
        val signal: Int

        when (info) {
            is CellInfoLte -> {
                cellId = info.cellIdentity.ci
                lac = info.cellIdentity.tac
                networkType = "LTE"
                signal = info.cellSignalStrength.dbm
            }
            is CellInfoGsm -> {
                cellId = info.cellIdentity.cid
                lac = info.cellIdentity.lac
                networkType = "GSM"
                signal = info.cellSignalStrength.dbm
            }
            is CellInfoWcdma -> {
                cellId = info.cellIdentity.cid
                lac = info.cellIdentity.lac
                networkType = "WCDMA"
                signal = info.cellSignalStrength.dbm
            }
            else -> return
        }

        if (cellId == Int.MAX_VALUE || cellId <= 0) return

        scope.launch {
            val knownCells = cellTowerDao.getKnownCellIds()
            var isAnomaly = false
            val reasons = mutableListOf<String>()

            // Check 1: Unknown cell tower
            if (knownCells.isNotEmpty() && cellId !in knownCells) {
                reasons.add("Unknown cell tower ID: $cellId")
                isAnomaly = true
            }

            // Check 2: Network downgrade (LTE → GSM = potential IMSI catcher)
            if (lastNetworkType == "LTE" && networkType == "GSM") {
                reasons.add("Network downgraded from LTE to GSM")
                isAnomaly = true
            }

            // Check 3: Sudden signal strength jump (> 20 dBm)
            if (lastSignalStrength != 0 && kotlin.math.abs(signal - lastSignalStrength) > 20) {
                reasons.add("Sudden signal jump: ${lastSignalStrength}dBm → ${signal}dBm")
                isAnomaly = true
            }

            lastNetworkType = networkType
            lastSignalStrength = signal

            val log = CellTowerLog(
                cellId = cellId,
                lac = lac,
                signalStrength = signal,
                networkType = networkType,
                isAnomaly = isAnomaly
            )
            cellTowerDao.insert(log)

            if (isAnomaly) {
                Log.w("CellTowerMonitor", "ANOMALY: ${reasons.joinToString()}")
                notificationManager.notifyImsiCatcher(cellId)
            }
        }
    }

    private fun checkSignalAnomaly(dbm: Int) {
        // Extremely strong signal from a close tower is suspicious
        if (dbm > -50 && dbm != 0) {
            scope.launch {
                notificationManager.notifyWarning(
                    "Unusually Strong Signal",
                    "Signal strength: ${dbm}dBm — could indicate a nearby surveillance device."
                )
            }
        }
    }
}
