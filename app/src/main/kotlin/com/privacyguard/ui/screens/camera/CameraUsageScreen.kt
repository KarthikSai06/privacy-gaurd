package com.privacyguard.ui.screens.camera

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyguard.data.db.entities.AppCameraUsage
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun CameraUsageScreen(
    onBack: () -> Unit,
    viewModel: CameraUsageViewModel = hiltViewModel()
) {
    val usageList by viewModel.usageList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "Camera Usage",
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = AccentAmber)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SectionHeader("APP CAMERA ACCESS (LAST 24H)") }

            if (usageList.isEmpty()) {
                item {
                    EmptyState(
                        message = "No camera access recorded recently",
                        icon = Icons.Filled.VideocamOff
                    )
                }
            } else {
                items(usageList, key = { it.id }) { usage ->
                    CameraUsageItem(usage = usage)
                }
            }
        }
    }
}

@Composable
fun CameraUsageItem(usage: AppCameraUsage) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentAmber.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(usage.appName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(usage.packageName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
                    Text("Last: ${fmt.format(Date(usage.lastAccessTime))}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
