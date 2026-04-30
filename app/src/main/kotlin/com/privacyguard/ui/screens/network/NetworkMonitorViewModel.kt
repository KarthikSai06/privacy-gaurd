package com.privacyguard.ui.screens.network

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.NetworkEvent
import com.privacyguard.data.repository.NetworkRepository
import com.privacyguard.service.NetworkMonitorVpnService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class NetworkUiState(
    val events: List<NetworkEvent> = emptyList(),
    val trackerCount: Int = 0,
    val isVpnActive: Boolean = false,
    val showTrackersOnly: Boolean = false
)

@HiltViewModel
class NetworkMonitorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkRepo: NetworkRepository
) : ViewModel() {

    private val _showTrackersOnly = MutableStateFlow(false)
    private val _vpnActive = MutableStateFlow(false)

    val state: StateFlow<NetworkUiState> = combine(
        _showTrackersOnly.flatMapLatest { trackersOnly ->
            if (trackersOnly) networkRepo.getTrackerEvents()
            else networkRepo.getAll()
        },
        networkRepo.countTodayTrackers(),
        _vpnActive,
        _showTrackersOnly
    ) { events, trackerCount, vpnActive, trackersOnly ->
        NetworkUiState(
            events = events,
            trackerCount = trackerCount,
            isVpnActive = vpnActive,
            showTrackersOnly = trackersOnly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkUiState())

    fun toggleFilter(trackersOnly: Boolean) {
        _showTrackersOnly.value = trackersOnly
    }

    /**
     * Returns the VpnService.prepare() intent if user consent is needed, or null if ready.
     */
    fun prepareVpn(): Intent? {
        return VpnService.prepare(context)
    }

    fun startVpn() {
        val intent = Intent(context, NetworkMonitorVpnService::class.java).apply {
            action = NetworkMonitorVpnService.ACTION_START
        }
        context.startForegroundService(intent)
        _vpnActive.value = true
    }

    fun stopVpn() {
        val intent = Intent(context, NetworkMonitorVpnService::class.java).apply {
            action = NetworkMonitorVpnService.ACTION_STOP
        }
        context.startService(intent)
        _vpnActive.value = false
    }
}
