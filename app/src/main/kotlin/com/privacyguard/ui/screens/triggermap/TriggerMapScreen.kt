package com.privacyguard.ui.screens.triggermap

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
import com.privacyguard.data.db.entities.TriggerPair
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TriggerMapScreen(
    onBack: () -> Unit,
    viewModel: TriggerMapViewModel = hiltViewModel()
) {
    val pairs by viewModel.pairs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "App Trigger Map",
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
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Detects when App B launches within 5 seconds of App A. " +
                            "Sorted by frequency — most suspicious pairs appear first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            item { SectionHeader("CO-ACTIVATION PAIRS (${pairs.size})") }

            if (pairs.isEmpty()) {
                item {
                    EmptyState(
                        message = "No trigger patterns detected yet.\nData is collected over time.",
                        icon = Icons.Filled.Timeline
                    )
                }
            } else {
                items(pairs, key = { it.id }) { pair ->
                    TriggerPairItem(pair = pair)
                }
            }
        }
    }
}

@Composable
fun TriggerPairItem(pair: TriggerPair) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // App A → App B row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App A
                Column(Modifier.weight(1f)) {
                    Text(pair.appAName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                    Text(pair.appA, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                // Arrow
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Text("5s", fontSize = 9.sp, color = TextMuted)
                }
                // App B
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(pair.appBName, fontWeight = FontWeight.SemiBold, color = AccentCyan, fontSize = 13.sp)
                    Text(pair.appB, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceElevated, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
            // Metadata row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                Text("Last: ${fmt.format(Date(pair.lastSeen))}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Repeat, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("×${pair.count}", fontWeight = FontWeight.Bold, color = AccentAmber, fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    PgBadge(if (pair.count >= 5) "HIGH" else "LOW", if (pair.count >= 5) AccentRed else AccentAmber)
                }
            }
        }
    }
}
