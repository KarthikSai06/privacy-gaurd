package com.privacyguard.ui.screens.clustering

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun ClusteringScreen(
    onBack: () -> Unit,
    viewModel: ClusteringViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "App Behavior Clusters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.analyze() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Re-analyze", tint = AccentCyan)
                }
            }

            if (state.isAnalyzing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentCyan)
                        Spacer(Modifier.height(16.dp))
                        Text("Analyzing ${state.totalApps} apps...", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Summary card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardDark,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.BubbleChart, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("K-Means App Clustering", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        "${state.totalApps} apps grouped into ${state.clusters.size} behavioral clusters",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Clusters
                    state.clusters.forEach { cluster ->
                        item {
                            Spacer(Modifier.height(12.dp))
                            val clusterColor = when {
                                cluster.avgRisk > 5f -> AccentRed
                                cluster.avgRisk > 2f -> AccentAmber
                                else -> AccentGreen
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = clusterColor.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Category, contentDescription = null, tint = clusterColor, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(cluster.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = clusterColor)
                                        Spacer(Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = clusterColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "${cluster.apps.size} apps",
                                                fontSize = 11.sp,
                                                color = clusterColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(cluster.apps.take(10)) { app ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CardDark,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Android, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(app.appName, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text(
                                        "%.1f".format(app.riskScore),
                                        fontSize = 12.sp,
                                        color = when {
                                            app.riskScore > 5f -> AccentRed
                                            app.riskScore > 2f -> AccentAmber
                                            else -> AccentGreen
                                        }
                                    )
                                }
                            }
                        }

                        if (cluster.apps.size > 10) {
                            item {
                                Text(
                                    "+${cluster.apps.size - 10} more apps",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
