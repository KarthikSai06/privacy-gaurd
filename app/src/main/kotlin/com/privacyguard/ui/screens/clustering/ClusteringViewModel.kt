package com.privacyguard.ui.screens.clustering

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.repository.*
import com.privacyguard.ml.AppClusterAnalyzer
import com.privacyguard.ml.FeatureExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppCluster(
    val clusterId: Int,
    val label: String,
    val apps: List<ClusteredApp>,
    val avgRisk: Float
)

data class ClusteredApp(
    val packageName: String,
    val appName: String,
    val riskScore: Float,
    val clusterId: Int
)

data class ClusteringUiState(
    val clusters: List<AppCluster> = emptyList(),
    val isAnalyzing: Boolean = false,
    val totalApps: Int = 0
)

@HiltViewModel
class ClusteringViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val micRepo: MicUsageRepository,
    private val cameraRepo: CameraUsageRepository,
    private val locationRepo: LocationUsageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClusteringUiState())
    val state: StateFlow<ClusteringUiState> = _state.asStateFlow()

    init {
        analyze()
    }

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            withContext(Dispatchers.Default) {
                try {
                    val pm = context.packageManager
                    val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        .filter { pm.getLaunchIntentForPackage(it.packageName) != null }

                    // Build feature vectors from available repo data
                    val micData = micRepo.getAllUsage().firstOrNull() ?: emptyList()
                    val cameraData = cameraRepo.getAllUsage().firstOrNull() ?: emptyList()
                    val locationData = locationRepo.getAllUsage().firstOrNull() ?: emptyList()

                    val appFeatures = installedApps.map { appInfo ->
                        val pkg = appInfo.packageName
                        val micCount = micData.count { it.packageName == pkg }.toFloat()
                        val camCount = cameraData.count { it.packageName == pkg }.toFloat()
                        val locCount = locationData.count { it.packageName == pkg }.toFloat()
                        val permCount = try {
                            pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
                                .requestedPermissions?.size?.toFloat() ?: 0f
                        } catch (e: Exception) { 0f }

                        pkg to floatArrayOf(micCount, camCount, locCount, permCount)
                    }

                    if (appFeatures.size < 3) {
                        _state.update { it.copy(isAnalyzing = false, totalApps = appFeatures.size) }
                        return@withContext
                    }

                    val k = minOf(4, appFeatures.size)
                    val analyzer = AppClusterAnalyzer()
                    val clustersResult = analyzer.cluster(appFeatures.toMap(), k = k)

                    // Map package names to their assigned cluster IDs
                    val packageToClusterId = clustersResult.flatMap { cluster ->
                        cluster.members.map { pkg -> pkg to cluster.id }
                    }.toMap()

                    val clusterLabels = listOf("Low Risk", "Moderate", "High Activity", "Suspicious")
                    val clusteredApps = appFeatures.map { (pkg, features) ->
                        ClusteredApp(
                            packageName = pkg,
                            appName = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { pkg },
                            riskScore = features.sum() / features.size,
                            clusterId = packageToClusterId[pkg] ?: -1
                        )
                    }

                    val clusters = (0 until k).map { cId ->
                        val apps = clusteredApps.filter { it.clusterId == cId }.sortedByDescending { it.riskScore }
                        AppCluster(
                            clusterId = cId,
                            label = clusterLabels.getOrElse(cId) { "Cluster $cId" },
                            apps = apps,
                            avgRisk = if (apps.isNotEmpty()) apps.map { it.riskScore }.average().toFloat() else 0f
                        )
                    }.filter { it.apps.isNotEmpty() }.sortedByDescending { it.avgRisk }

                    _state.update { it.copy(
                        clusters = clusters,
                        isAnalyzing = false,
                        totalApps = appFeatures.size
                    )}
                } catch (e: Exception) {
                    _state.update { it.copy(isAnalyzing = false) }
                }
            }
        }
    }
}
