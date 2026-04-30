package com.privacyguard.ui.screens.appdetail

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.*
import com.privacyguard.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AppDetailUiState(
    val packageName: String = "",
    val appName: String = "",
    val micEvents: List<AppMicUsage> = emptyList(),
    val cameraEvents: List<AppCameraUsage> = emptyList(),
    val locationEvents: List<AppLocationUsage> = emptyList(),
    val nightEvents: List<NightActivity> = emptyList(),
    val networkEvents: List<NetworkEvent> = emptyList(),
    val triggerPairs: List<TriggerPair> = emptyList(),
    val isKeylogger: Boolean = false,
    val anomalyScore: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    micRepo: MicUsageRepository,
    cameraRepo: CameraUsageRepository,
    locationRepo: LocationUsageRepository,
    nightRepo: NightActivityRepository,
    accessibilityRepo: AccessibilityRepository,
    networkRepo: NetworkRepository,
    triggerRepo: TriggerPairRepository
) : ViewModel() {

    private val pkg: String = savedStateHandle.get<String>("packageName") ?: ""

    private val appName: String = try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(pkg, 0)
        pm.getApplicationLabel(appInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        pkg
    }

    val state: StateFlow<AppDetailUiState> = combine(
        micRepo.getAllUsage().map { list -> list.filter { it.packageName == pkg } },
        cameraRepo.getAllUsage().map { list -> list.filter { it.packageName == pkg } },
        locationRepo.getAllUsage().map { list -> list.filter { it.packageName == pkg } },
        nightRepo.getAll().map { list -> list.filter { it.packageName == pkg } },
        accessibilityRepo.getAll().map { list -> list.any { it.packageName == pkg && it.isSuspicious } }
    ) { mic, cam, loc, night, isKl ->
        val penalty = (if (isKl) 30 else 0) + (night.size * 5) + (cam.size * 5) + (loc.size * 3) + (mic.size * 2)
        val score = maxOf(0, 100 - penalty)
        AppDetailUiState(
            packageName = pkg,
            appName = appName,
            micEvents = mic,
            cameraEvents = cam,
            locationEvents = loc,
            nightEvents = night,
            isKeylogger = isKl,
            anomalyScore = score,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppDetailUiState(packageName = pkg, appName = appName))
}
