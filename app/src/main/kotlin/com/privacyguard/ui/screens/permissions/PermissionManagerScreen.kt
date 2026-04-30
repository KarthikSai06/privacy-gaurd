package com.privacyguard.ui.screens.permissions

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*

@Composable
fun PermissionManagerScreen(
    onBack: () -> Unit,
    viewModel: PermissionManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(title = "Permission Audit", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentAmber.copy(0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "${state.filteredApps.size} apps with dangerous permissions",
                                fontWeight = FontWeight.Bold, color = AccentAmber
                            )
                            Text(
                                "${state.totalDangerousGrants} total dangerous grants",
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder = { Text("Search apps…", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceElevated,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            item { SectionHeader("APPS (${state.filteredApps.size})") }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
            } else if (state.filteredApps.isEmpty()) {
                item {
                    EmptyState(
                        message = "No apps found with dangerous permissions.",
                        icon = Icons.Filled.VerifiedUser
                    )
                }
            } else {
                items(state.filteredApps, key = { it.packageName }) { app ->
                    PermissionAppCard(app) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionAppCard(app: AppPermissionInfo, onClick: () -> Unit) {
    val dangerColor = when {
        app.dangerousCount >= 5 -> AccentRed
        app.dangerousCount >= 3 -> AccentAmber
        else -> AccentGreen
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(dangerColor.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${app.dangerousCount}",
                    fontWeight = FontWeight.Bold,
                    color = dangerColor,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                // Permission chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    app.permissions.take(4).forEach { perm ->
                        val shortName = perm.replace("_", " ").take(8)
                        val chipColor = when {
                            perm.contains("CAMERA") || perm.contains("RECORD_AUDIO") -> AccentRed
                            perm.contains("LOCATION") -> AccentAmber
                            else -> AccentCyan
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = chipColor.copy(0.12f)) {
                            Text(
                                shortName,
                                color = chipColor,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (app.permissions.size > 4) {
                        Surface(shape = RoundedCornerShape(4.dp), color = TextMuted.copy(0.15f)) {
                            Text(
                                "+${app.permissions.size - 4}",
                                color = TextMuted,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
