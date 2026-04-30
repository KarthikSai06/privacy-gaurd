package com.privacyguard.ui.screens.permissions

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppPermissionInfo(
    val packageName: String,
    val appName: String,
    val permissions: List<String>,
    val dangerousCount: Int
)

data class PermissionUiState(
    val apps: List<AppPermissionInfo> = emptyList(),
    val filteredApps: List<AppPermissionInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val totalDangerousGrants: Int = 0
)

@HiltViewModel
class PermissionManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private val DANGEROUS_PERMISSIONS = setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.CALL_PHONE",
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.BODY_SENSORS"
        )
    }

    private val _state = MutableStateFlow(PermissionUiState())
    val state: StateFlow<PermissionUiState> = _state.asStateFlow()

    init { loadApps() }

    fun search(query: String) {
        _state.update { current ->
            val filtered = if (query.isBlank()) current.apps
            else current.apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
            current.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val apps = withContext(Dispatchers.IO) { queryInstalledApps() }
            val totalDangerous = apps.sumOf { it.dangerousCount }
            _state.update {
                it.copy(
                    apps = apps,
                    filteredApps = apps,
                    isLoading = false,
                    totalDangerousGrants = totalDangerous
                )
            }
        }
    }

    private fun queryInstalledApps(): List<AppPermissionInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        return packages
            .filter { pkg ->
                // Exclude system apps unless they have dangerous permissions
                val isUser = (pkg.applicationInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_SYSTEM) ?: 0) == 0
                isUser || hasDangerousPermissions(pkg)
            }
            .mapNotNull { pkg ->
                val granted = pkg.requestedPermissions?.filterIndexed { index, _ ->
                    (pkg.requestedPermissionsFlags?.getOrNull(index) ?: 0) and
                            PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
                }?.filter { it in DANGEROUS_PERMISSIONS } ?: emptyList()

                if (granted.isEmpty()) return@mapNotNull null

                val appName = pm.getApplicationLabel(
                    pkg.applicationInfo ?: return@mapNotNull null
                ).toString()

                AppPermissionInfo(
                    packageName = pkg.packageName,
                    appName = appName,
                    permissions = granted.map { it.removePrefix("android.permission.") },
                    dangerousCount = granted.size
                )
            }
            .sortedByDescending { it.dangerousCount }
    }

    private fun hasDangerousPermissions(pkg: PackageInfo): Boolean {
        return pkg.requestedPermissions?.any { it in DANGEROUS_PERMISSIONS } == true
    }
}
