package com.privacyguard.ui.screens.phishing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.dao.PhishingAlertDao
import com.privacyguard.data.db.entities.PhishingAlert
import com.privacyguard.ml.PhishingDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhishingUiState(
    val alerts: List<PhishingAlert> = emptyList(),
    val phishingCount: Int = 0,
    val isLoading: Boolean = false,
    val manualScanResult: PhishingDetector.PhishingResult? = null
)

@HiltViewModel
class PhishingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phishingAlertDao: PhishingAlertDao
) : ViewModel() {

    private val _state = MutableStateFlow(PhishingUiState())
    val state: StateFlow<PhishingUiState> = _state.asStateFlow()

    private val detector = PhishingDetector(context)

    init { loadAlerts() }

    private fun loadAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            phishingAlertDao.getAll().collect { alerts ->
                _state.update { it.copy(
                    alerts = alerts,
                    phishingCount = alerts.count { a -> a.isPhishing },
                    isLoading = false
                )}
            }
        }
    }

    fun manualScan(text: String) {
        viewModelScope.launch {
            val result = detector.analyze(text)
            _state.update { it.copy(manualScanResult = result) }

            // Persist
            phishingAlertDao.insert(PhishingAlert(
                source = "MANUAL_SCAN",
                senderOrApp = "User Input",
                content = text.take(200),
                detectedUrl = result.detectedUrl,
                riskScore = result.riskScore,
                isPhishing = result.isPhishing
            ))
        }
    }

    fun clearResult() {
        _state.update { it.copy(manualScanResult = null) }
    }
}
