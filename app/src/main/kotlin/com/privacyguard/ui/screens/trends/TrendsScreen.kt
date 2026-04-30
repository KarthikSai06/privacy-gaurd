package com.privacyguard.ui.screens.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    onBack: () -> Unit,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Trends") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current score card
            item {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Privacy Score", style = MaterialTheme.typography.titleMedium)
                        Text("${state.currentScore}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                state.currentScore >= 70 -> Color(0xFF4CAF50)
                                state.currentScore >= 40 -> Color(0xFFFFA726)
                                else -> Color.Red
                            })
                        val changeText = if (state.scoreChange >= 0) "+${state.scoreChange}" else "${state.scoreChange}"
                        val changeColor = if (state.scoreChange >= 0) Color(0xFF4CAF50) else Color.Red
                        Text("$changeText pts vs last week", color = changeColor,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Score chart
            item {
                Card {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("30-Day Score History", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.scores.size >= 2) {
                            val scores = state.scores.reversed().map { it.overallScore.toFloat() }
                            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                val width = size.width
                                val height = size.height
                                val maxScore = 100f
                                val stepX = width / (scores.size - 1).coerceAtLeast(1)

                                // Grid lines
                                for (i in 0..4) {
                                    val y = height - (height * (i * 25f / maxScore))
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1f
                                    )
                                }

                                // Line path
                                val path = Path()
                                scores.forEachIndexed { index, score ->
                                    val x = index * stepX
                                    val y = height - (height * (score / maxScore))
                                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, Color(0xFF6200EA), style = Stroke(width = 3f))

                                // Points
                                scores.forEachIndexed { index, score ->
                                    val x = index * stepX
                                    val y = height - (height * (score / maxScore))
                                    drawCircle(Color(0xFF6200EA), radius = 4f, center = Offset(x, y))
                                }
                            }
                        } else {
                            Text("Not enough data yet. Check back in a few days!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Average", "${state.avgScore}", Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("Best", "${state.bestScore}", Color(0xFF4CAF50), Modifier.weight(1f))
                    StatCard("Worst", "${state.worstScore}", Color(0xFFF44336), Modifier.weight(1f))
                }
            }

            // Recent scores
            item {
                Text("Recent Scores", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            state.scores.take(10).forEach { score ->
                item {
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(score.date, fontWeight = FontWeight.Bold)
                                Text("${score.appsScanned} apps scanned, ${score.threatsDetected} threats",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${score.overallScore}/100",
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    score.overallScore >= 70 -> Color(0xFF4CAF50)
                                    score.overallScore >= 40 -> Color(0xFFFFA726)
                                    else -> Color.Red
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
