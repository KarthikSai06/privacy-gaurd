package com.privacyguard.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.repository.AccessibilityRepository
import com.privacyguard.data.repository.CameraUsageRepository
import com.privacyguard.data.repository.LocationUsageRepository
import com.privacyguard.data.repository.MicUsageRepository
import com.privacyguard.data.repository.NightActivityRepository
import com.privacyguard.data.repository.TriggerPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveIncident(
    val id: String,
    val appName: String,
    val packageName: String,
    val type: String, // "Mic", "Camera", "Location"
    val timestamp: Long
)

data class DashboardUiState(
    val micAppsToday: Int = 0,
    val cameraAppsToday: Int = 0,
    val locationAppsToday: Int = 0,
    val suspiciousApps: Int = 0,
    val nightEvents: Int = 0,
    val triggerPairs: Int = 0,
    val privacyScore: Int = 100,
    val recentIncidents: List<LiveIncident> = emptyList(),
    val lastScanTime: Long = 0L,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val nightRepo: NightActivityRepository,
    private val triggerRepo: TriggerPairRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _lastScanTime = MutableStateFlow(System.currentTimeMillis())

    private val recentIncidentsFlow = combine(
        micRepo.getAllUsage(),
        cameraRepo.getAllUsage(),
        locationRepo.getAllUsage()
    ) { micList, camList, locList ->
        val merged = mutableListOf<LiveIncident>()
        merged.addAll(micList.map { LiveIncident("mic_${it.id}", it.appName, it.packageName, "Mic", it.lastAccessTime) })
        merged.addAll(camList.map { LiveIncident("cam_${it.id}", it.appName, it.packageName, "Camera", it.lastAccessTime) })
        merged.addAll(locList.map { LiveIncident("loc_${it.id}", it.appName, it.packageName, "Location", it.lastAccessTime) })
        
        merged.sortedByDescending { it.timestamp }.take(5)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        micRepo.countTodayApps(),
        cameraRepo.countTodayApps(),
        locationRepo.countTodayApps(),
        accessibilityRepo.countSuspicious(),
        nightRepo.countThisWeek(),
        triggerRepo.count(),
        recentIncidentsFlow,
        _isRefreshing,
        _lastScanTime
    ) { flows ->
        val mic = flows[0] as Int
        val cam = flows[1] as Int
        val loc = flows[2] as Int
        val acc = flows[3] as Int
        val night = flows[4] as Int
        val trig = flows[5] as Int
        @Suppress("UNCHECKED_CAST")
        val incidents = flows[6] as List<LiveIncident>
        val refreshing = flows[7] as Boolean
        val scanTime = flows[8] as Long
        
        // Equal baseline penalty weights logic as discussed
        val penalty = (acc * 20) + (night * 5) + (cam * 5) + (loc * 5) + (mic * 5)
        val score = maxOf(0, 100 - penalty)
        
        DashboardUiState(
            micAppsToday = mic,
            cameraAppsToday = cam,
            locationAppsToday = loc,
            suspiciousApps = acc,
            nightEvents = night,
            triggerPairs = trig,
            privacyScore = score,
            recentIncidents = incidents,
            lastScanTime = scanTime,
            isRefreshing = refreshing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                micRepo.scanAndStore()
                cameraRepo.scanAndStore()
                locationRepo.scanAndStore()
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
