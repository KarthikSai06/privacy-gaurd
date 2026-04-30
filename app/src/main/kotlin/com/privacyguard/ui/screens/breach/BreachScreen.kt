package com.privacyguard.ui.screens.breach

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun BreachScreen(
    onBack: () -> Unit,
    viewModel: BreachViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var emailInput by remember { mutableStateOf("") }

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
                    "Data Breach Monitor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.GppBad, contentDescription = null, tint = AccentRed, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Email input card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardDark,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Check if your email has been compromised",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = TextMuted,
                                    focusedLabelColor = AccentCyan,
                                    unfocusedLabelColor = TextMuted,
                                    cursorColor = AccentCyan,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.checkBreaches(emailInput) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                enabled = emailInput.contains("@") && !state.isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    contentColor = PrimaryDark
                                )
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryDark
                                    )
                                } else {
                                    Icon(Icons.Filled.Search, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Check Breaches", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Error
                if (state.error != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentRed.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Error, contentDescription = null, tint = AccentRed)
                                Spacer(Modifier.width(8.dp))
                                Text(state.error!!, color = AccentRed, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Results
                if (state.hasChecked && state.error == null) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        if (state.breaches.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AccentGreen.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("No breaches found!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                        Text("Your email appears safe.", fontSize = 13.sp, color = TextSecondary)
                                    }
                                }
                            }
                        } else {
                            SectionHeader("${state.breaches.size} BREACHES FOUND")
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    items(state.breaches) { breach ->
                        BreachCard(breach)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BreachCard(breach: BreachInfo) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(breach.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Domain: ", fontSize = 12.sp, color = TextMuted)
                Text(breach.domain, fontSize = 12.sp, color = AccentCyan)
                Spacer(Modifier.width(16.dp))
                Text("Date: ", fontSize = 12.sp, color = TextMuted)
                Text(breach.breachDate, fontSize = 12.sp, color = AccentAmber)
            }
            Spacer(Modifier.height(8.dp))
            Text("Exposed data:", fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                breach.dataClasses.take(4).forEach { dc ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            dc, fontSize = 10.sp, color = AccentRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (breach.dataClasses.size > 4) {
                    Text("+${breach.dataClasses.size - 4}", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}
