package com.privacyguard.ui.screens.network

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.data.db.entities.NetworkEvent
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NetworkMonitorScreen(
    onBack: () -> Unit,
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "Network Monitor", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // VPN toggle card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (state.isVpnActive) AccentCyan.copy(0.1f) else SurfaceCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (state.isVpnActive) AccentCyan.copy(0.2f)
                                    else TextMuted.copy(0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (state.isVpnActive) AccentCyan else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (state.isVpnActive) "VPN Monitor Active" else "VPN Monitor Off",
                                fontWeight = FontWeight.Bold,
                                color = if (state.isVpnActive) AccentCyan else TextPrimary
                            )
                            Text(
                                "Local DNS inspection only — no data leaves your device",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = state.isVpnActive,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    val prepareIntent = viewModel.prepareVpn()
                                    if (prepareIntent != null) {
                                        vpnLauncher.launch(prepareIntent)
                                    } else {
                                        viewModel.startVpn()
                                    }
                                } else {
                                    viewModel.stopVpn()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentCyan,
                                checkedTrackColor = AccentCyan.copy(0.3f)
                            )
                        )
                    }
                }
            }

            // Tracker count banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.trackerCount > 0) AccentRed.copy(0.1f) else AccentGreen.copy(0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (state.trackerCount > 0) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (state.trackerCount > 0) AccentRed else AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (state.trackerCount > 0)
                                "${state.trackerCount} tracker domain(s) contacted today"
                            else
                                "No tracker domains detected today",
                            fontWeight = FontWeight.Medium,
                            color = if (state.trackerCount > 0) AccentRed else AccentGreen,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Filter chips
            item {
                SectionHeader("FILTER")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !state.showTrackersOnly,
                        onClick = { viewModel.toggleFilter(false) },
                        label = { Text("All DNS Queries") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                    FilterChip(
                        selected = state.showTrackersOnly,
                        onClick = { viewModel.toggleFilter(true) },
                        label = { Text("Trackers Only") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed.copy(0.2f),
                            selectedLabelColor = AccentRed
                        )
                    )
                }
            }

            item { SectionHeader("DNS QUERIES (${state.events.size})") }

            if (state.events.isEmpty()) {
                item {
                    EmptyState(
                        message = if (state.isVpnActive)
                            "Monitoring DNS queries… Results will appear here."
                        else
                            "Enable the VPN monitor above to start tracking DNS queries.",
                        icon = Icons.Filled.Dns
                    )
                }
            } else {
                items(state.events, key = { it.id }) { event ->
                    NetworkEventItem(event)
                }
            }
        }
    }
}

@Composable
fun NetworkEventItem(event: NetworkEvent) {
    val color = if (event.isTracker) AccentRed else AccentGreen
    val fmt = SimpleDateFormat("h:mm:ss a", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (event.isTracker) Icons.Filled.GppBad else Icons.Filled.Dns,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.domain, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 13.sp)
                Text(event.appName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (event.isTracker) {
                    PgBadge("TRACKER", AccentRed)
                    Spacer(Modifier.height(2.dp))
                }
                Text(fmt.format(Date(event.timestamp)), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}
