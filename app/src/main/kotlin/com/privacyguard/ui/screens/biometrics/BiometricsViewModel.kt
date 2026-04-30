package com.privacyguard.ui.screens.biometrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.dao.KeystrokeProfileDao
import com.privacyguard.data.db.entities.KeystrokeProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BiometricsUiState(
    val ownerProfiles: List<KeystrokeProfile> = emptyList(),
    val intruderProfiles: List<KeystrokeProfile> = emptyList(),
    val isEnrolled: Boolean = false,
    val intruderAlerts: Int = 0,
    val confidenceScore: Float = 0f
)

@HiltViewModel
class BiometricsViewModel @Inject constructor(
    private val keystrokeDao: KeystrokeProfileDao
) : ViewModel() {

    private val _state = MutableStateFlow(BiometricsUiState())
    val state: StateFlow<BiometricsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                keystrokeDao.getOwnerProfiles(),
                keystrokeDao.getIntruderProfiles()
            ) { owners, intruders ->
                val total = owners.size + intruders.size
                BiometricsUiState(
                    ownerProfiles = owners,
                    intruderProfiles = intruders,
                    isEnrolled = owners.size >= 5,
                    intruderAlerts = intruders.size,
                    confidenceScore = if (total > 0) owners.size.toFloat() / total else 0f
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            keystrokeDao.deleteAll()
        }
    }

    /**
     * Records a typing sample to build the owner's biometric profile.
     * Since Android blocks raw keystroke timings globally, we estimate
     * dwell/flight from an in-app typing session.
     */
    fun recordTypingSample(charCount: Int, durationMs: Long) {
        if (charCount < 10) return // Ignore very short samples

        // Estimate metrics based on the session
        val avgFlightTime = (durationMs.toFloat() / charCount) * 0.8f // 80% flight
        val avgDwellTime = (durationMs.toFloat() / charCount) * 0.2f  // 20% dwell
        val cpm = (charCount.toFloat() / (durationMs / 60000f))

        val profile = KeystrokeProfile(
            avgDwellTime = avgDwellTime,
            avgFlightTime = avgFlightTime,
            typingSpeed = cpm,
            sessionDuration = durationMs,
            isOwner = true
        )

        viewModelScope.launch {
            keystrokeDao.insert(profile)
        }
    }
}
