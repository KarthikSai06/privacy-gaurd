package com.privacyguard.ui.screens.appdetail

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun AppDetailScreen(
    onBack: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val scoreColor = when {
        state.anomalyScore >= 80 -> AccentGreen
        state.anomalyScore >= 50 -> AccentAmber
        else -> AccentRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "App Detail", onBack = onBack)

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App header
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(scoreColor.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${state.anomalyScore}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = scoreColor
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(state.appName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text(state.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (state.isKeylogger) PgBadge("KEYLOGGER", AccentRed)
                            PgBadge(
                                when {
                                    state.anomalyScore >= 80 -> "SAFE"
                                    state.anomalyScore >= 50 -> "MODERATE"
                                    else -> "HIGH RISK"
                                },
                                scoreColor
                            )
                        }
                    }
                }
            }

            // Open system settings button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${state.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open App Info in Settings")
            }

            SectionHeader("PRIVACY EVENT BREAKDOWN")

            // Stat rows
            DetailStatRow(Icons.Filled.Mic, "Mic accesses", state.micEvents.size, AccentCyan)
            DetailStatRow(Icons.Filled.CameraAlt, "Camera accesses", state.cameraEvents.size, AccentAmber)
            DetailStatRow(Icons.Filled.LocationOn, "Location queries", state.locationEvents.size, AccentGreen)
            DetailStatRow(Icons.Filled.Nightlight, "Night events", state.nightEvents.size, AccentAmber)
            DetailStatRow(Icons.Filled.GppBad, "Keylogger", if (state.isKeylogger) 1 else 0, AccentRed)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailStatRow(icon: ImageVector, label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(
                value.toString(),
                color = if (value > 0) color else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
