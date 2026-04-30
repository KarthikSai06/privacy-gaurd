package com.privacyguard.ui.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.dao.DailyScoreDao
import com.privacyguard.data.db.entities.DailyScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrendsUiState(
    val scores: List<DailyScore> = emptyList(),
    val currentScore: Int = 0,
    val scoreChange: Int = 0,       // +/- vs last week
    val avgScore: Int = 0,
    val bestScore: Int = 0,
    val worstScore: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val dailyScoreDao: DailyScoreDao
) : ViewModel() {

    private val _state = MutableStateFlow(TrendsUiState())
    val state: StateFlow<TrendsUiState> = _state.asStateFlow()

    init { loadTrends() }

    private fun loadTrends() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            dailyScoreDao.getRecent(30).collect { scores ->
                val current = scores.firstOrNull()?.overallScore ?: 0
                val weekAgo = scores.getOrNull(7)?.overallScore ?: current
                val avg = if (scores.isNotEmpty()) scores.map { it.overallScore }.average().toInt() else 0
                val best = scores.maxOfOrNull { it.overallScore } ?: 0
                val worst = scores.minOfOrNull { it.overallScore } ?: 0

                _state.update { it.copy(
                    scores = scores,
                    currentScore = current,
                    scoreChange = current - weekAgo,
                    avgScore = avg,
                    bestScore = best,
                    worstScore = worst,
                    isLoading = false
                )}
            }
        }
    }
}
