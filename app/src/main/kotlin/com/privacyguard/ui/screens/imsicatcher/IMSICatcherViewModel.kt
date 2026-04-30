package com.privacyguard.ui.screens.imsicatcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.dao.CellTowerDao
import com.privacyguard.data.db.entities.CellTowerLog
import com.privacyguard.service.CellTowerMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IMSICatcherUiState(
    val recentLogs: List<CellTowerLog> = emptyList(),
    val anomalyCount: Int = 0,
    val isMonitoring: Boolean = false,
    val lastAnalysis: String = "Not yet analyzed",
    val suspiciousPatterns: List<String> = emptyList()
)

@HiltViewModel
class IMSICatcherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cellTowerDao: CellTowerDao,
    private val cellTowerMonitor: CellTowerMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(IMSICatcherUiState())
    val state: StateFlow<IMSICatcherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cellTowerDao.getRecent(50).collect { logs ->
                val anomalies = logs.count { it.isAnomaly }
                val patterns = mutableListOf<String>()
                if (anomalies > 0) {
                    // Check for rapid cell tower changes
                    val uniqueCells = logs.map { it.cellId }.distinct().size
                    if (uniqueCells > 10) patterns.add("Rapid cell tower switching detected (${uniqueCells} towers)")
                    // Check for signal strength anomalies
                    val strongSignals = logs.filter { it.signalStrength > -50 }
                    if (strongSignals.isNotEmpty()) patterns.add("Unusually strong signals detected (${strongSignals.size} events)")
                    // Check for LAC changes
                    val uniqueLacs = logs.map { it.lac }.distinct().size
                    if (uniqueLacs > 5) patterns.add("Multiple LAC changes detected (${uniqueLacs} areas)")
                }
                _state.update { it.copy(
                    recentLogs = logs,
                    anomalyCount = anomalies,
                    suspiciousPatterns = patterns,
                    lastAnalysis = if (logs.isNotEmpty()) "Last: ${logs.first().timestamp}" else "No data"
                )}
            }
        }
    }

    fun toggleMonitoring() {
        val willMonitor = !_state.value.isMonitoring
        _state.update { it.copy(isMonitoring = willMonitor) }
        
        if (willMonitor) {
            cellTowerMonitor.startMonitoring()
        } else {
            cellTowerMonitor.stopMonitoring()
        }
    }
}
