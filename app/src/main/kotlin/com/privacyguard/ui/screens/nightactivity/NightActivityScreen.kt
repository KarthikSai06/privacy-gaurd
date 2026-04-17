package com.privacyguard.ui.screens.nightactivity

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.data.db.entities.NightActivity
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NightActivityScreen(
    onBack: () -> Unit,
    viewModel: NightActivityViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsState()
    val filterDays by viewModel.filterDays.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "Night Activity",
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filter chips
            item {
                SectionHeader("FILTER BY DATE RANGE")
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Today", 3 to "3 Days", 7 to "7 Days", 30 to "30 Days").forEach { (days, label) ->
                        FilterChip(
                            selected = filterDays == days,
                            onClick = { viewModel.setFilterDays(days) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentAmber.copy(0.2f),
                                selectedLabelColor = AccentAmber
                            )
                        )
                    }
                }
            }

            // Info banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentAmber.copy(0.1f)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Nightlight, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${activities.size} apps active between 1 AM – 5 AM",
                            fontWeight = FontWeight.Medium,
                            color = AccentAmber
                        )
                    }
                }
            }

            item { SectionHeader("SUSPICIOUS APPS (1AM – 5AM)") }

            if (activities.isEmpty()) {
                item {
                    EmptyState(
                        message = "No suspicious night-time app activity detected.\nYou're all clear! 🌙",
                        icon = Icons.Filled.Bedtime
                    )
                }
            } else {
                items(activities, key = { it.id }) { activity ->
                    NightActivityItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun NightActivityItem(activity: NightActivity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentAmber.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Nightlight, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(activity.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                val timeFmt = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())
                Text(
                    timeFmt.format(Date(activity.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            PgBadge("NIGHT", AccentAmber)
        }
    }
}
