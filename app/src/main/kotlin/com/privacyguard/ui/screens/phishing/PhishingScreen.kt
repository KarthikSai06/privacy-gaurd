package com.privacyguard.ui.screens.phishing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishingScreen(
    onBack: () -> Unit,
    viewModel: PhishingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phishing Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats banner
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Phishing Detected", style = MaterialTheme.typography.titleMedium)
                            Text("${state.phishingCount} threats found",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Security, contentDescription = null,
                            modifier = Modifier.size(48.dp))
                    }
                }
            }

            // Manual scan input
            item {
                Card {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Scan Text for Phishing", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("Paste SMS, URL, or message...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { if (inputText.isNotBlank()) viewModel.manualScan(inputText) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze")
                        }
                    }
                }
            }

            // Scan result
            state.manualScanResult?.let { result ->
                item {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = if (result.isPhishing) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                    )) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                if (result.isPhishing) "⚠️ PHISHING DETECTED" else "✅ APPEARS SAFE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (result.isPhishing) Color.Red else Color(0xFF2E7D32)
                            )
                            Text("Risk Score: ${(result.riskScore * 100).toInt()}%")
                            if (result.detectedUrl.isNotBlank()) {
                                Text("URL: ${result.detectedUrl}", style = MaterialTheme.typography.bodySmall)
                            }
                            result.reasons.forEach { reason ->
                                Text("• $reason", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Alert history
            item {
                Text("Alert History", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            items(state.alerts) { alert ->
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                Card {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Icon(
                            if (alert.isPhishing) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (alert.isPhishing) Color.Red else Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(alert.source, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(alert.content.take(100),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2)
                            Text(dateFormat.format(Date(alert.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${(alert.riskScore * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = if (alert.isPhishing) Color.Red else Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}
