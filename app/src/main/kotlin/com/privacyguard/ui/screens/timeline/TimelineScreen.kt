package com.privacyguard.ui.screens.timeline

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineScreen(
    onBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "Privacy Timeline", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date filter chips
            item {
                SectionHeader("DATE RANGE")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Today", 3 to "3 Days", 7 to "7 Days", 30 to "30 Days").forEach { (days, label) ->
                        FilterChip(
                            selected = state.filterDays == days,
                            onClick = { viewModel.setFilterDays(days) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan.copy(0.2f),
                                selectedLabelColor = AccentCyan
                            )
                        )
                    }
                }
            }

            // Event type filter chips
            item {
                SectionHeader("EVENT TYPE")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val types = listOf(null to "All", "MIC" to "Mic", "CAMERA" to "Camera", "LOCATION" to "Location")
                    types.forEach { (type, label) ->
                        val color = getEventColor(type)
                        FilterChip(
                            selected = state.filterType == type,
                            onClick = { viewModel.setFilterType(type) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(0.2f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }
            }

            item {
                SectionHeader("EVENTS (${state.events.size})")
            }

            if (state.events.isEmpty()) {
                item {
                    EmptyState(
                        message = "No privacy events in this time range.\nTap Scan Now on the dashboard to collect data.",
                        icon = Icons.Filled.Timeline
                    )
                }
            } else {
                items(state.events, key = { it.id }) { event ->
                    TimelineEventItem(event)
                }
            }
        }
    }
}

@Composable
fun TimelineEventItem(event: TimelineEvent) {
    val (icon, color) = getEventIconAndColor(event.eventType)
    val fmt = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline dot
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                Text(event.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                PgBadge(event.eventType, color)
                Spacer(Modifier.height(2.dp))
                Text(fmt.format(Date(event.timestamp)), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}

private fun getEventColor(type: String?): Color = when (type) {
    "MIC" -> AccentCyan
    "CAMERA" -> AccentAmber
    "LOCATION" -> AccentGreen
    "NIGHT" -> AccentAmber
    "KEYLOGGER" -> AccentRed
    "TRIGGER" -> AccentCyan
    else -> AccentCyan
}

private fun getEventIconAndColor(type: String): Pair<ImageVector, Color> = when (type) {
    "MIC" -> Icons.Filled.Mic to AccentCyan
    "CAMERA" -> Icons.Filled.CameraAlt to AccentAmber
    "LOCATION" -> Icons.Filled.LocationOn to AccentGreen
    "NIGHT" -> Icons.Filled.Nightlight to AccentAmber
    "KEYLOGGER" -> Icons.Filled.GppBad to AccentRed
    "TRIGGER" -> Icons.Filled.Timeline to AccentCyan
    else -> Icons.Filled.Info to AccentCyan
}
