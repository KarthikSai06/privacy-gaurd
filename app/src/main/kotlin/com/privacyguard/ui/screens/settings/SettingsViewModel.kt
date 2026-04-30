package com.privacyguard.ui.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.AppDatabase
import com.privacyguard.utils.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val nightStartHour: Int = 1,
    val nightEndHour: Int = 5,
    val micAlertThresholdMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val trustedPackages: Set<String> = emptySet(),
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val isClearing: Boolean = false,
    val clearSuccess: Boolean = false,
    val exportError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
    @ApplicationContext private val context: Context,
    private val dataExportManager: DataExportManager,
    private val database: AppDatabase
) : ViewModel() {

    companion object {
        val KEY_NIGHT_START = intPreferencesKey("night_start")
        val KEY_NIGHT_END = intPreferencesKey("night_end")
        val KEY_MIC_THRESHOLD = intPreferencesKey("mic_threshold")
        val KEY_NOTIFS = booleanPreferencesKey("notifications_enabled")
        val KEY_TRUSTED = stringSetPreferencesKey("trusted_packages")
    }

    private val _exportState = MutableStateFlow(
        SettingsState()  // merge with dataStore below
    )

    val settings: StateFlow<SettingsState> = combine(
        dataStore.data.map { prefs ->
            SettingsState(
                nightStartHour = prefs[KEY_NIGHT_START] ?: 1,
                nightEndHour = prefs[KEY_NIGHT_END] ?: 5,
                micAlertThresholdMinutes = prefs[KEY_MIC_THRESHOLD] ?: 5,
                notificationsEnabled = prefs[KEY_NOTIFS] ?: true,
                trustedPackages = prefs[KEY_TRUSTED] ?: emptySet()
            )
        },
        _exportState
    ) { prefState, exportState ->
        prefState.copy(
            isExporting = exportState.isExporting,
            exportSuccess = exportState.exportSuccess,
            isClearing = exportState.isClearing,
            clearSuccess = exportState.clearSuccess,
            exportError = exportState.exportError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setNightHours(start: Int, end: Int) = viewModelScope.launch {
        dataStore.edit { it[KEY_NIGHT_START] = start; it[KEY_NIGHT_END] = end }
    }

    fun setMicThreshold(minutes: Int) = viewModelScope.launch {
        dataStore.edit { it[KEY_MIC_THRESHOLD] = minutes }
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_NOTIFS] = enabled }
    }

    fun addTrustedPackage(pkg: String) = viewModelScope.launch {
        dataStore.edit { prefs ->
            val current = prefs[KEY_TRUSTED] ?: emptySet()
            prefs[KEY_TRUSTED] = current + pkg
        }
    }

    fun removeTrustedPackage(pkg: String) = viewModelScope.launch {
        dataStore.edit { prefs ->
            val current = prefs[KEY_TRUSTED] ?: emptySet()
            prefs[KEY_TRUSTED] = current - pkg
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _exportState.update { it.copy(isExporting = true, exportSuccess = false, exportError = null) }
            try {
                val file = dataExportManager.exportToZip(context)
                dataExportManager.share(context, file)
                _exportState.update { it.copy(isExporting = false, exportSuccess = true) }
            } catch (e: Exception) {
                _exportState.update { it.copy(isExporting = false, exportError = e.message) }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _exportState.update { it.copy(isClearing = true, clearSuccess = false) }
            try {
                database.clearAllTables()
                _exportState.update { it.copy(isClearing = false, clearSuccess = true) }
            } catch (_: Exception) {
                _exportState.update { it.copy(isClearing = false) }
            }
        }
    }
}
