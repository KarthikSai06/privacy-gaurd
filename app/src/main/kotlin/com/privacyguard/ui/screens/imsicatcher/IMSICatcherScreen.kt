package com.privacyguard.ui.screens.imsicatcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun IMSICatcherScreen(
    onBack: () -> Unit,
    viewModel: IMSICatcherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        
        if (locationGranted && phoneStateGranted) {
            viewModel.toggleMonitoring()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "IMSI Catcher Detection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.CellTower, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Status card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardDark,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusColor = if (state.anomalyCount == 0) AccentGreen else AccentRed
                                val statusIcon = if (state.anomalyCount == 0) Icons.Filled.CheckCircle else Icons.Filled.Warning
                                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        if (state.anomalyCount == 0) "No Threats Detected" else "${state.anomalyCount} Anomalies Found",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                    Text(
                                        "Monitoring cell tower behavior for fake base stations",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    if (state.isMonitoring) {
                                        viewModel.toggleMonitoring()
                                    } else {
                                        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        val hasPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                                        if (hasLocation && hasPhoneState) {
                                            viewModel.toggleMonitoring()
                                        } else {
                                            permissionLauncher.launch(arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.READ_PHONE_STATE
                                            ))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isMonitoring) AccentRed else AccentCyan,
                                    contentColor = PrimaryDark
                                )
                            ) {
                                Icon(
                                    if (state.isMonitoring) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (state.isMonitoring) "Stop Monitoring" else "Start Monitoring",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Suspicious patterns
                if (state.suspiciousPatterns.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        SectionHeader("SUSPICIOUS PATTERNS")
                        Spacer(Modifier.height(8.dp))
                    }
                    items(state.suspiciousPatterns) { pattern ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentRed.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(pattern, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                // Recent tower logs
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader("CELL TOWER LOGS (${state.recentLogs.size})")
                    Spacer(Modifier.height(8.dp))
                }

                if (state.recentLogs.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.CellTower, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No cell tower data yet", color = TextMuted, fontSize = 14.sp)
                                Text("Start monitoring to collect data", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                items(state.recentLogs) { log ->
                    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (log.isAnomaly) AccentRed.copy(alpha = 0.08f) else CardDark,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (log.isAnomaly) Icons.Filled.Warning else Icons.Filled.CellTower,
                                contentDescription = null,
                                tint = if (log.isAnomaly) AccentRed else AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CID: ${log.cellId}  LAC: ${log.lac}", fontSize = 13.sp, color = TextPrimary)
                                Text("Signal: ${log.signalStrength} dBm • ${log.networkType}", fontSize = 11.sp, color = TextMuted)
                            }
                            Text(fmt.format(Date(log.timestamp)), fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
