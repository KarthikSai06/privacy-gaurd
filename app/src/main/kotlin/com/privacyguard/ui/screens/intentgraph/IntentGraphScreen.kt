package com.privacyguard.ui.screens.intentgraph

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
fun IntentGraphScreen(
    onBack: () -> Unit,
    viewModel: IntentGraphViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("App Intent Graph", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.analyze() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Re-analyze", tint = AccentCyan)
                }
            }

            if (state.isAnalyzing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentCyan)
                        Spacer(Modifier.height(16.dp))
                        Text("Mapping inter-app communications...", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Summary
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = CardDark,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Apps", fontSize = 12.sp, color = TextMuted)
                                    Text("${state.nodes.size}", fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold, color = AccentCyan)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Connections", fontSize = 12.sp, color = TextMuted)
                                    Text("${state.totalConnections}", fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold, color = AccentAmber)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Suspicious", fontSize = 12.sp, color = TextMuted)
                                    val sc = state.suspiciousChains.size
                                    Text("$sc", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                        color = if (sc > 0) AccentRed else AccentGreen)
                                }
                            }
                        }
                    }

                    // Suspicious chains
                    if (state.suspiciousChains.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            SectionHeader("SUSPICIOUS PATTERNS")
                            Spacer(Modifier.height(8.dp))
                        }
                        items(state.suspiciousChains) { chain ->
                            val sevColor = when (chain.severity) {
                                "HIGH" -> AccentRed; "MEDIUM" -> AccentAmber; else -> AccentCyan
                            }
                            Surface(shape = RoundedCornerShape(12.dp),
                                color = sevColor.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Warning, contentDescription = null,
                                            tint = sevColor, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(chain.severity, fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold, color = sevColor)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(chain.description, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Top connected apps
                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeader("MOST CONNECTED APPS")
                        Spacer(Modifier.height(8.dp))
                    }
                    val topNodes = state.nodes
                        .sortedByDescending { it.connectionCount }.take(20)
                    items(topNodes) { node ->
                        Surface(shape = RoundedCornerShape(10.dp), color = CardDark,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccountTree, contentDescription = null,
                                    tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(node.appName, fontSize = 13.sp, color = TextPrimary)
                                    Text("${node.connectionCount} connections",
                                        fontSize = 11.sp, color = TextMuted)
                                }
                                val rc = when {
                                    node.connectionCount > 20 -> AccentRed
                                    node.connectionCount > 10 -> AccentAmber
                                    else -> AccentGreen
                                }
                                Surface(shape = RoundedCornerShape(6.dp),
                                    color = rc.copy(alpha = 0.15f)) {
                                    Text("${node.connectionCount}", fontSize = 12.sp,
                                        color = rc, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
