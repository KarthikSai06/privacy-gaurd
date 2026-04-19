package com.privacyguard.ui.screens.aiinsights

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ml.AppFeatures
import com.privacyguard.ui.components.EmptyState
import com.privacyguard.ui.components.PgTopBar
import com.privacyguard.ui.components.SectionHeader
import com.privacyguard.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AiInsightsScreen(
    onBack: () -> Unit,
    viewModel: AiInsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "AI Anomaly Insights",
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.analyze() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Re-analyze", tint = AccentCyan)
                }
            }
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // Info banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentCyan.copy(0.08f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                if (state.usingFallback) "Rule-Based Engine (Active)" else "TFLite Model (Active)",
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan,
                                fontSize = 13.sp
                            )
                            Text(
                                "Apps are scored 0–100% anomaly. 0% = normal, 100% = highly suspicious.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item { SectionHeader("APP ANOMALY RANKINGS") }

            if (state.appFeatures.isEmpty()) {
                item {
                    EmptyState(
                        message = "No data yet. Tap Scan Now on the dashboard first.",
                        icon = Icons.Filled.Psychology
                    )
                }
            } else {
                items(state.appFeatures, key = { it.packageName }) { app ->
                    AiAppCard(app)
                }
            }
        }
    }
}

@Composable
fun AiAppCard(app: AppFeatures) {
    val pct = (app.anomalyScore * 100f).roundToInt()
    val color = when {
        pct >= 70 -> AccentRed
        pct >= 40 -> AccentAmber
        else      -> AccentGreen
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$pct%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.appName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(app.packageName, color = TextMuted, fontSize = 11.sp)
                }
                // Badge
                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(0.15f)) {
                    Text(
                        when {
                            pct >= 70 -> "HIGH RISK"
                            pct >= 40 -> "MODERATE"
                            else -> "NORMAL"
                        },
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Anomaly progress bar
            LinearProgressIndicator(
                progress = { app.anomalyScore },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Color.White.copy(0.05f)
            )

            Spacer(Modifier.height(10.dp))

            // Feature breakdown chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (app.micCount > 0)       FeatureChip("🎤 ${app.micCount}", AccentCyan)
                if (app.cameraCount > 0)    FeatureChip("📷 ${app.cameraCount}", AccentAmber)
                if (app.locationCount > 0)  FeatureChip("📍 ${app.locationCount}", AccentGreen)
                if (app.nightCount > 0)     FeatureChip("🌙 ${app.nightCount}", AccentAmber)
                if (app.isKeylogger)        FeatureChip("⌨️ KL", AccentRed)
                if (app.triggerCount > 0)   FeatureChip("🔗 ${app.triggerCount}", AccentCyan)
            }
        }
    }
}

@Composable
fun FeatureChip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(0.1f)) {
        Text(text, color = color, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}
