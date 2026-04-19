package com.privacyguard.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigateMic: () -> Unit,
    onNavigateCamera: () -> Unit,
    onNavigateLocation: () -> Unit,
    onNavigateKeylogger: () -> Unit,
    onNavigateNight: () -> Unit,
    onNavigateTrigger: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateReport: () -> Unit = {},
    onNavigateAi: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("PrivacyGuard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                PulseDot(color = AccentGreen, modifier = Modifier.padding(end = 4.dp))
                Spacer(Modifier.width(4.dp))
                Text("Live", fontSize = 11.sp, color = AccentGreen)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onNavigateSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Last scan info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    Text(
                        "Last scan: ${if (state.lastScanTime > 0) fmt.format(Date(state.lastScanTime)) else "Never"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(Modifier.weight(1f))
                    if (state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AccentCyan)
                    }
                }

                // ── Privacy Score Ring ──────────────────────────────────────
                PrivacyScoreCard(score = state.privacyScore)
                Spacer(Modifier.height(20.dp))

                // Recent incidents strip
                if (state.recentIncidents.isNotEmpty()) {
                    SectionHeader("RECENT INCIDENTS")
                    Spacer(Modifier.height(8.dp))
                    state.recentIncidents.take(3).forEach { incident ->
                        IncidentChip(incident)
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                SectionHeader("PRIVACY OVERVIEW")
                Spacer(Modifier.height(8.dp))

                StatCard(
                    icon = Icons.Filled.Mic,
                    label = "Apps used mic today",
                    value = state.micAppsToday.toString(),
                    accentColor = AccentCyan,
                    onClick = onNavigateMic
                )
                Spacer(Modifier.height(10.dp))
                StatCard(
                    icon = Icons.Filled.CameraAlt,
                    label = "Apps used camera today",
                    value = state.cameraAppsToday.toString(),
                    accentColor = AccentAmber,
                    onClick = onNavigateCamera
                )
                Spacer(Modifier.height(10.dp))
                StatCard(
                    icon = Icons.Filled.LocationOn,
                    label = "Apps queried location today",
                    value = state.locationAppsToday.toString(),
                    accentColor = AccentGreen,
                    onClick = onNavigateLocation
                )
                Spacer(Modifier.height(10.dp))
                StatCard(
                    icon = Icons.Filled.Accessible,
                    label = "Suspicious accessibility apps",
                    value = state.suspiciousApps.toString(),
                    accentColor = if (state.suspiciousApps > 0) AccentRed else AccentGreen,
                    onClick = onNavigateKeylogger
                )
                Spacer(Modifier.height(10.dp))
                StatCard(
                    icon = Icons.Filled.Nightlight,
                    label = "Night-time events (7 days)",
                    value = state.nightEvents.toString(),
                    accentColor = AccentAmber,
                    onClick = onNavigateNight
                )
                Spacer(Modifier.height(10.dp))
                StatCard(
                    icon = Icons.Filled.Timeline,
                    label = "Trigger patterns detected",
                    value = state.triggerPairs.toString(),
                    accentColor = AccentCyan,
                    onClick = onNavigateTrigger
                )

                Spacer(Modifier.height(24.dp))
                SectionHeader("QUICK ACTIONS")
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = PrimaryDark),
                        enabled = !state.isRefreshing
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Scan Now", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Settings")
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onNavigateReport,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentAmber)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PDF Report")
                    }
                    OutlinedButton(
                        onClick = onNavigateAi,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("AI Insights")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PrivacyScoreCard(score: Int) {
    val scoreColor = when {
        score >= 80 -> AccentGreen
        score >= 50 -> AccentAmber
        else -> AccentRed
    }
    val label = when {
        score >= 80 -> "Good"
        score >= 50 -> "Fair"
        else -> "At Risk"
    }

    val animatedSweep by animateFloatAsState(
        targetValue = (score / 100f) * 270f,
        animationSpec = tween(durationMillis = 1200),
        label = "score_arc"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arc ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    val inset = 5.dp.toPx()
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    val topLeft = Offset(inset, inset)
                    // Background track
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                    // Score arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(scoreColor.copy(0.6f), scoreColor),
                            center = Offset(size.width / 2f, size.height / 2f)
                        ),
                        startAngle = 135f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                }
                Text(
                    text = "$score",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text("Privacy Score", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        score >= 80 -> "Your device appears safe."
                        score >= 50 -> "Some threats detected — review."
                        else -> "Immediate attention required!"
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun IncidentChip(incident: LiveIncident) {
    val (icon, color) = when (incident.type) {
        "Mic" -> Icons.Filled.Mic to AccentCyan
        "Camera" -> Icons.Filled.CameraAlt to AccentAmber
        "Location" -> Icons.Filled.LocationOn to AccentGreen
        else -> Icons.Filled.Warning to AccentRed
    }
    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(incident.appName, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(fmt.format(Date(incident.timestamp)), color = TextMuted, fontSize = 11.sp)
        }
    }
}
