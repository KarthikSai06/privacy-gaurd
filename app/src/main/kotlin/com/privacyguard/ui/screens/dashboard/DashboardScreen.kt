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

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
