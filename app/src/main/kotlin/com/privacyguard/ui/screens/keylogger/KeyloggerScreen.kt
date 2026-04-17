package com.privacyguard.ui.screens.keylogger

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.data.db.entities.AccessibilityRecord
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KeyloggerScreen(
    onBack: () -> Unit,
    viewModel: KeyloggerViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsState()
    val suspiciousCount by viewModel.suspiciousCount.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "Keylogger Detector",
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
            // Summary banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (suspiciousCount > 0) AccentRed.copy(0.1f) else AccentGreen.copy(0.1f)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (suspiciousCount > 0) Icons.Filled.GppBad else Icons.Filled.GppGood,
                            contentDescription = null,
                            tint = if (suspiciousCount > 0) AccentRed else AccentGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (suspiciousCount > 0) "$suspiciousCount Suspicious App(s) Found" else "No Suspicious Apps",
                                fontWeight = FontWeight.Bold,
                                color = if (suspiciousCount > 0) AccentRed else AccentGreen
                            )
                            Text(
                                "${records.size} total accessibility services active",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Accessibility Settings")
                }
            }

            item { SectionHeader("ACCESSIBILITY SERVICES") }

            if (records.isEmpty()) {
                item {
                    EmptyState(
                        message = "No accessibility services found.\nTap refresh to scan.",
                        icon = Icons.Filled.Shield
                    )
                }
            } else {
                items(records, key = { it.packageName }) { record ->
                    AccessibilityRecordItem(record = record)
                }
            }
        }
    }
}

@Composable
fun AccessibilityRecordItem(record: AccessibilityRecord) {
    val isSafe = !record.isSuspicious
    val accentColor = if (isSafe) BadgeSafe else BadgeDanger

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSafe) Icons.Filled.VerifiedUser else Icons.Filled.GppBad,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(record.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(record.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(2.dp))
                val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                Text(
                    "First seen: ${fmt.format(Date(record.firstDetectedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.width(8.dp))
            PgBadge(if (isSafe) "SAFE" else "SUSPICIOUS", accentColor)
        }
    }
}
