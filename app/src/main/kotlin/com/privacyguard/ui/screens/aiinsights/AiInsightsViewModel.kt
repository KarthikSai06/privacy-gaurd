package com.privacyguard.ui.screens.aiinsights

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.ml.AnomalyDetector
import com.privacyguard.ml.AppFeatures
import com.privacyguard.ml.FeatureExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiInsightsUiState(
    val appFeatures: List<AppFeatures> = emptyList(),
    val isLoading: Boolean = false,
    val usingFallback: Boolean = true   // true = rule-based, false = TFLite
)

@HiltViewModel
class AiInsightsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureExtractor: FeatureExtractor
) : ViewModel() {

    private val _state = MutableStateFlow(AiInsightsUiState())
    val state: StateFlow<AiInsightsUiState> = _state.asStateFlow()

    private val detector = AnomalyDetector(context)

    init { analyze() }

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = featureExtractor.extractAll(detector)
            _state.update { it.copy(appFeatures = result, isLoading = false) }
        }
    }
}
