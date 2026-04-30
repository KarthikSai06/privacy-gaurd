package com.privacyguard.ui.screens.intentgraph

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.ml.IntentGraphAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppNodeUi(
    val packageName: String,
    val appName: String,
    val connectionCount: Int
)

data class IntentGraphUiState(
    val nodes: List<AppNodeUi> = emptyList(),
    val suspiciousChains: List<IntentGraphAnalyzer.SuspiciousChain> = emptyList(),
    val isAnalyzing: Boolean = false,
    val totalConnections: Int = 0
)

@HiltViewModel
class IntentGraphViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(IntentGraphUiState())
    val state: StateFlow<IntentGraphUiState> = _state.asStateFlow()

    private val analyzer = IntentGraphAnalyzer(context)

    init { analyze() }

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            withContext(Dispatchers.Default) {
                try {
                    val graph = analyzer.analyze()
                    // Compute connection count per node from edges
                    val edgeCounts = mutableMapOf<String, Int>()
                    graph.edges.forEach { e ->
                        edgeCounts[e.from] = (edgeCounts[e.from] ?: 0) + 1
                        edgeCounts[e.to] = (edgeCounts[e.to] ?: 0) + 1
                    }
                    val uiNodes = graph.nodes.map { n ->
                        AppNodeUi(n.packageName, n.appName, edgeCounts[n.packageName] ?: 0)
                    }
                    _state.update { it.copy(
                        nodes = uiNodes,
                        suspiciousChains = graph.suspiciousChains,
                        isAnalyzing = false,
                        totalConnections = graph.edges.size
                    )}
                } catch (e: Exception) {
                    _state.update { it.copy(isAnalyzing = false) }
                }
            }
        }
    }
}
