package com.privacyguard.ui.screens.biometrics

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun BiometricsScreen(
    onBack: () -> Unit,
    viewModel: BiometricsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Behavioral Biometrics", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Fingerprint, contentDescription = null,
                    tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Status card
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = CardDark,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = if (state.isEnrolled) Icons.Filled.VerifiedUser else Icons.Filled.PersonAdd
                                val color = if (state.isEnrolled) AccentGreen else AccentAmber
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        if (state.isEnrolled) "Profile Enrolled" else "Not Enrolled",
                                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color
                                    )
                                    Text(
                                        "Keystroke dynamics intruder detection",
                                        fontSize = 12.sp, color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = CardDark,
                            modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${state.ownerProfiles.size}", fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold, color = AccentCyan)
                                Text("Samples", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Surface(shape = RoundedCornerShape(14.dp), color = CardDark,
                            modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${state.intruderAlerts}", fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.intruderAlerts > 0) AccentRed else AccentGreen)
                                Text("Intruder Alerts", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Surface(shape = RoundedCornerShape(14.dp), color = CardDark,
                            modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.0f".format(state.confidenceScore * 100)}%",
                                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                                Text("Confidence", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }
                
                // Training Area (Only show if not fully enrolled)
                if (!state.isEnrolled) {
                    item {
                        var trainingText by remember { mutableStateOf("") }
                        var startTime by remember { mutableStateOf(0L) }

                        Spacer(Modifier.height(16.dp))
                        SectionHeader("ENROLLMENT TRAINING (${state.ownerProfiles.size}/5)")
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp), 
                            color = CardDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Type the following phrase naturally to build your profile:\n\"The quick brown fox jumps over the lazy dog.\"",
                                    fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp)
                                )
                                OutlinedTextField(
                                    value = trainingText,
                                    onValueChange = { newText ->
                                        if (trainingText.isEmpty() && newText.isNotEmpty()) {
                                            startTime = System.currentTimeMillis()
                                        }
                                        trainingText = newText
                                        
                                        // Auto-submit after ~40 chars
                                        if (newText.length >= 40 && startTime > 0) {
                                            val duration = System.currentTimeMillis() - startTime
                                            viewModel.recordTypingSample(newText.length, duration)
                                            trainingText = "" // Reset for next sample
                                            startTime = 0L
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Start typing here...", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = SurfaceElevated,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                val progress = state.ownerProfiles.size / 5f
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = progress.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = AccentCyan,
                                    trackColor = SurfaceElevated
                                )
                            }
                        }
                    }
                }

                // How it works
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader("HOW IT WORKS")
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(14.dp), color = CardDark,
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val steps = listOf(
                                "Monitors your typing rhythm via AccessibilityService",
                                "Builds a statistical profile of key hold/flight times",
                                "Compares each session against your baseline profile",
                                "Alerts if someone else is using your device"
                            )
                            steps.forEachIndexed { i, step ->
                                Row(modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top) {
                                    Text("${i + 1}.", fontSize = 13.sp, color = AccentCyan,
                                        fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(step, fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // Reset button
                item {
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetProfile() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset Biometric Profile")
                    }
                }
            }
        }
    }
}
