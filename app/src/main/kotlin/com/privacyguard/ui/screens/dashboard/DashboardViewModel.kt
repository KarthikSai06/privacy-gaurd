package com.privacyguard.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.repository.AccessibilityRepository
import com.privacyguard.data.repository.MicUsageRepository
import com.privacyguard.data.repository.NightActivityRepository
import com.privacyguard.data.repository.TriggerPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val micAppsToday: Int = 0,
    val suspiciousApps: Int = 0,
    val nightEvents: Int = 0,
    val triggerPairs: Int = 0,
    val lastScanTime: Long = 0L,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val micRepo: MicUsageRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val nightRepo: NightActivityRepository,
    private val triggerRepo: TriggerPairRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _lastScanTime = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<DashboardUiState> = combine(
        micRepo.countTodayApps(),
        accessibilityRepo.countSuspicious(),
        nightRepo.countThisWeek(),
        triggerRepo.count(),
        _isRefreshing
    ) { mic, acc, night, trig, refreshing ->
        DashboardUiState(
            micAppsToday = mic,
            suspiciousApps = acc,
            nightEvents = night,
            triggerPairs = trig,
            lastScanTime = _lastScanTime.value,
            isRefreshing = refreshing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                micRepo.scanAndStore()
                accessibilityRepo.scanAndStore()
                nightRepo.scanAndStore()
                triggerRepo.scanAndStore()
                _lastScanTime.value = System.currentTimeMillis()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
