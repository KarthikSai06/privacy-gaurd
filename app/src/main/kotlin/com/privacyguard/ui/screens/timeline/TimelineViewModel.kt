package com.privacyguard.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TimelineEvent(
    val id: String,
    val packageName: String,
    val appName: String,
    val eventType: String,   // MIC, CAMERA, LOCATION, NIGHT, KEYLOGGER, TRIGGER
    val timestamp: Long,
    val details: String = ""
)

data class TimelineUiState(
    val events: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = false,
    val filterType: String? = null,   // null = all
    val filterDays: Int = 7
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository,
    private val nightRepo: NightActivityRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val triggerRepo: TriggerPairRepository
) : ViewModel() {

    private val _filterType = MutableStateFlow<String?>(null)
    private val _filterDays = MutableStateFlow(7)

    val state: StateFlow<TimelineUiState> = combine(
        _filterDays,
        _filterType,
        micRepo.getAllUsage(),
        cameraRepo.getAllUsage(),
        locationRepo.getAllUsage()
    ) { days, filterType, micList, camList, locList ->
        val since = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        val merged = mutableListOf<TimelineEvent>()

        micList.filter { it.lastAccessTime >= since }.forEach {
            merged.add(TimelineEvent("mic_${it.id}", it.packageName, it.appName, "MIC", it.lastAccessTime))
        }
        camList.filter { it.lastAccessTime >= since }.forEach {
            merged.add(TimelineEvent("cam_${it.id}", it.packageName, it.appName, "CAMERA", it.lastAccessTime))
        }
        locList.filter { it.lastAccessTime >= since }.forEach {
            merged.add(TimelineEvent("loc_${it.id}", it.packageName, it.appName, "LOCATION", it.lastAccessTime))
        }

        val filtered = if (filterType != null) merged.filter { it.eventType == filterType }
        else merged

        TimelineUiState(
            events = filtered.sortedByDescending { it.timestamp },
            filterType = filterType,
            filterDays = days
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimelineUiState(isLoading = true))

    fun setFilterType(type: String?) { _filterType.value = type }
    fun setFilterDays(days: Int) { _filterDays.value = days }
}
