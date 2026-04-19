package com.privacyguard.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

                // Privacy Score Overview
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val scoreColor = when {
                        state.privacyScore > 80 -> AccentGreen
                        state.privacyScore > 50 -> AccentAmber
                        else -> AccentRed
                    }
                    CircularProgressIndicator(
                        progress = state.privacyScore / 100f,
                        modifier = Modifier.size(120.dp),
                        color = scoreColor,
                        strokeWidth = 8.dp,
                        trackColor = SurfaceLight
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.privacyScore}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Score", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                SectionHeader(title = "SURVEILLANCE PROTECTION", icon = Icons.Filled.Security)
                Spacer(Modifier.height(8.dp))
                SensorGridBlock(
                    state = state,
                    onNavigateMic = onNavigateMic,
                    onNavigateCamera = onNavigateCamera,
                    onNavigateLocation = onNavigateLocation
                )

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "BEHAVIORAL ANALYSIS", icon = Icons.Filled.Analytics)
                Spacer(Modifier.height(8.dp))
                
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
                SectionHeader("LIVE INCIDENTS")
                Spacer(Modifier.height(8.dp))

                if (state.recentIncidents.isEmpty()) {
                    Text("No recent tracking incidents detected.", color = TextMuted, fontSize = 14.sp)
                } else {
                    state.recentIncidents.forEach { incident ->
                        LiveIncidentItem(incident = incident)
                        Spacer(Modifier.height(10.dp))
                    }
                }

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

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LiveIncidentItem(incident: LiveIncident) {
    val (icon, color) = when (incident.type) {
        "Mic" -> Icons.Filled.Mic to AccentCyan
        "Camera" -> Icons.Filled.CameraAlt to AccentAmber
        else -> Icons.Filled.LocationOn to AccentCyan // Location
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(0.12f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(incident.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                val fmt = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
                Text("Used ${incident.type} at ${fmt.format(Date(incident.timestamp))}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun SensorGridBlock(
    state: DashboardUiState,
    onNavigateMic: () -> Unit,
    onNavigateCamera: () -> Unit,
    onNavigateLocation: () -> Unit
) {
    val totalUsage = state.micAppsToday + state.cameraAppsToday + state.locationAppsToday
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total Sensor Usage", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(totalUsage.toString(), color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SensorSquare(
                    icon = Icons.Filled.CameraAlt,
                    label = "Camera\nUsage",
                    value = state.cameraAppsToday.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateCamera
                )
                SensorSquare(
                    icon = Icons.Filled.Mic,
                    label = "Mic\nUsage",
                    value = state.micAppsToday.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateMic
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SensorSquare(
                    icon = Icons.Filled.LocationOn,
                    label = "Location\nUsage",
                    value = state.locationAppsToday.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateLocation
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* Placeholder for block feature */ },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight, contentColor = TextPrimary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Blocking Options", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorSquare(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = SurfaceLight
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = TextPrimary, fontSize = 12.sp, lineHeight = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = AccentAmber, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
