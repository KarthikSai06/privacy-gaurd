package com.privacyguard.ui.screens.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.repository.*
import com.privacyguard.domain.PrivacyScoreCalculator
import com.privacyguard.utils.PdfReportGenerator
import com.privacyguard.utils.ReportData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReportUiState(
    val score: Int = 100,
    val scoreLabel: String = "Good",
    val micApps: Int = 0,
    val cameraApps: Int = 0,
    val locationApps: Int = 0,
    val suspiciousKeyloggers: Int = 0,
    val nightEvents: Int = 0,
    val triggerPairs: Int = 0,
    val isGenerating: Boolean = false,
    val generatedFilePath: String? = null,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository,
    private val accessibilityRepo: AccessibilityRepository,
    private val nightRepo: NightActivityRepository,
    private val triggerRepo: TriggerPairRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                micRepo.countTodayApps(),
                cameraRepo.countTodayApps(),
                locationRepo.countTodayApps(),
                accessibilityRepo.countSuspicious(),
                nightRepo.countThisWeek(),
                triggerRepo.count()
            ) { arr ->
                val mic = arr[0] as Int
                val cam = arr[1] as Int
                val loc = arr[2] as Int
                val kl  = arr[3] as Int
                val ngt = arr[4] as Int
                val trg = arr[5] as Int
                val breakdown = PrivacyScoreCalculator.calculate(kl, ngt, cam, loc, mic, trg)
                ReportUiState(
                    score = breakdown.score,
                    scoreLabel = breakdown.label,
                    micApps = mic,
                    cameraApps = cam,
                    locationApps = loc,
                    suspiciousKeyloggers = kl,
                    nightEvents = ngt,
                    triggerPairs = trg
                )
            }.collect { _state.value = it }
        }
    }

    fun generatePdf() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            try {
                val cal = Calendar.getInstance()
                val weekEnd = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val weekStart = cal.timeInMillis

                val s = _state.value
                val data = ReportData(
                    score = s.score,
                    scoreLabel = s.scoreLabel,
                    micApps = s.micApps,
                    cameraApps = s.cameraApps,
                    locationApps = s.locationApps,
                    suspiciousKeyloggers = s.suspiciousKeyloggers,
                    nightEvents = s.nightEvents,
                    triggerPairs = s.triggerPairs,
                    weekStart = weekStart,
                    weekEnd = weekEnd
                )
                val file = PdfReportGenerator.generate(context, data)
                _state.update { it.copy(isGenerating = false, generatedFilePath = file.absolutePath) }
                PdfReportGenerator.share(context, file)
            } catch (e: Exception) {
                _state.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }
}
