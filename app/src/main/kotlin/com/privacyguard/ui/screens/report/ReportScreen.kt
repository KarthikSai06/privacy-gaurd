package com.privacyguard.ui.screens.report

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.PgTopBar
import com.privacyguard.ui.components.SectionHeader
import com.privacyguard.ui.theme.*

@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val scoreColor = when {
        state.score >= 80 -> AccentGreen
        state.score >= 50 -> AccentAmber
        else -> AccentRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "Weekly Privacy Report", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Score banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = scoreColor.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Privacy Score", fontSize = 14.sp, color = TextMuted)
                    Spacer(Modifier.height(6.dp))
                    Text("${state.score}", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = scoreColor)
                    Text(state.scoreLabel, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = scoreColor)
                }
            }

            SectionHeader("THIS WEEK'S BREAKDOWN")

            ReportStatRow(Icons.Filled.Mic,        "Mic accesses",        state.micApps,               AccentCyan)
            ReportStatRow(Icons.Filled.CameraAlt,  "Camera accesses",     state.cameraApps,             AccentAmber)
            ReportStatRow(Icons.Filled.LocationOn, "Location queries",    state.locationApps,           AccentGreen)
            ReportStatRow(Icons.Filled.GppBad,     "Suspicious keyloggers", state.suspiciousKeyloggers, AccentRed)
            ReportStatRow(Icons.Filled.Nightlight, "Night-time events",   state.nightEvents,            AccentAmber)
            ReportStatRow(Icons.Filled.Timeline,   "Trigger patterns",    state.triggerPairs,           AccentCyan)

            Spacer(Modifier.height(8.dp))

            if (state.error != null) {
                Text(
                    "Error: ${state.error}",
                    color = AccentRed,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { viewModel.generatePdf() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber, contentColor = PrimaryDark)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryDark)
                    Spacer(Modifier.width(10.dp))
                    Text("Generating PDF…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Generate & Share PDF Report", fontWeight = FontWeight.Bold)
                }
            }

            if (state.generatedFilePath != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = AccentGreen.copy(0.1f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Report saved & shared successfully!", color = AccentGreen, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportStatRow(icon: ImageVector, label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(value.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
