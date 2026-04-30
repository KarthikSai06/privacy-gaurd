package com.privacyguard.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showAddTrusted by remember { mutableStateOf(false) }
    var newTrustedPkg by remember { mutableStateOf("") }
    var nightStart by remember(settings.nightStartHour) { mutableIntStateOf(settings.nightStartHour) }
    var nightEnd by remember(settings.nightEndHour) { mutableIntStateOf(settings.nightEndHour) }
    var micThreshold by remember(settings.micAlertThresholdMinutes) { mutableIntStateOf(settings.micAlertThresholdMinutes) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "Settings", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notifications toggle
            item {
                SectionHeader("NOTIFICATIONS")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Privacy Alerts", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Notify on suspicious app detection", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = AccentCyan.copy(0.3f))
                        )
                    }
                }
            }

            // Night hours
            item {
                SectionHeader("NIGHT HOURS")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Start Hour: $nightStart:00", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Slider(
                        value = nightStart.toFloat(),
                        onValueChange = { nightStart = it.toInt() },
                        onValueChangeFinished = { viewModel.setNightHours(nightStart, nightEnd) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("End Hour: $nightEnd:00", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Slider(
                        value = nightEnd.toFloat(),
                        onValueChange = { nightEnd = it.toInt() },
                        onValueChangeFinished = { viewModel.setNightHours(nightStart, nightEnd) },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                    )
                    Text(
                        "Flagging apps active between $nightStart:00 AM – $nightEnd:00 AM",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Mic threshold
            item {
                SectionHeader("MIC USAGE THRESHOLD")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Alert if mic used for > $micThreshold minutes", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Slider(
                        value = micThreshold.toFloat(),
                        onValueChange = { micThreshold = it.toInt() },
                        onValueChangeFinished = { viewModel.setMicThreshold(micThreshold) },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )
                    Text(
                        "Current threshold: $micThreshold min",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // ── DATA MANAGEMENT ────────────────────────────────────────
            item {
                SectionHeader("DATA MANAGEMENT")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Export button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Export Data (CSV)", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Download all privacy logs as a ZIP", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.exportData() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !settings.isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = PrimaryDark)
                    ) {
                        if (settings.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryDark)
                            Spacer(Modifier.width(8.dp))
                            Text("Exporting…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Data", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (settings.exportSuccess) {
                        Spacer(Modifier.height(6.dp))
                        Text("✅ Export shared successfully!", color = AccentGreen, fontSize = 12.sp)
                    }
                    if (settings.exportError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("❌ ${settings.exportError}", color = AccentRed, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceElevated, thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    // Clear data button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = AccentRed, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Clear All Data", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Permanently delete all recorded privacy logs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Clear All Data", fontWeight = FontWeight.Bold)
                    }

                    if (settings.clearSuccess) {
                        Spacer(Modifier.height(6.dp))
                        Text("✅ All data cleared.", color = AccentGreen, fontSize = 12.sp)
                    }
                }
            }

            // Trusted apps whitelist
            item {
                SectionHeader("TRUSTED APP WHITELIST")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Trusted Packages", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("These apps won't be flagged as suspicious", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        IconButton(onClick = { showAddTrusted = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = AccentCyan)
                        }
                    }
                    if (settings.trustedPackages.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("No trusted packages added", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        settings.trustedPackages.forEach { pkg ->
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(pkg, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.removeTrustedPackage(pkg) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = AccentRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // About
            item {
                SectionHeader("ABOUT")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("PrivacyGuard v2.0", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("All data stored locally. Network monitor uses local-only VPN.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Supports Android 8.0 (API 26)+", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Add trusted package dialog
    if (showAddTrusted) {
        AlertDialog(
            onDismissRequest = { showAddTrusted = false; newTrustedPkg = "" },
            containerColor = SurfaceCard,
            title = { Text("Add Trusted Package", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTrustedPkg,
                    onValueChange = { newTrustedPkg = it },
                    label = { Text("Package name (e.g. com.example.app)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        focusedLabelColor = AccentCyan,
                        cursorColor = AccentCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTrustedPkg.isNotBlank()) {
                        viewModel.addTrustedPackage(newTrustedPkg.trim())
                        newTrustedPkg = ""
                        showAddTrusted = false
                    }
                }) { Text("Add", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showAddTrusted = false; newTrustedPkg = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Clear data confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SurfaceCard,
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentRed) },
            title = { Text("Clear All Data?", color = TextPrimary) },
            text = { Text("This will permanently delete all recorded privacy logs, events, and scan history. This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearDialog = false
                }) { Text("Clear", color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
