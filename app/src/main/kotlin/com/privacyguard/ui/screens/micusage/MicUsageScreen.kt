package com.privacyguard.ui.screens.micusage

import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.privacyguard.data.db.entities.AppMicUsage
import com.privacyguard.ui.components.*
import com.privacyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun MicUsageScreen(
    onBack: () -> Unit,
    viewModel: MicUsageViewModel = hiltViewModel()
) {
    val usageList by viewModel.usageList.collectAsState()
    val alertApps by viewModel.alertApps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        PgTopBar(
            title = "Microphone Usage",
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Alert banner
            if (alertApps.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentRed.copy(alpha = 0.12f)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("${alertApps.size} app(s) used mic for >5 min", fontWeight = FontWeight.Bold, color = AccentRed)
                                Text("Check below for details", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Chart
            if (usageList.isNotEmpty()) {
                item {
                    SectionHeader("HOURLY ACTIVITY (24H)")
                    MicBarChart(usageList = usageList)
                }
            }

            // List
            item { SectionHeader("APP MIC ACCESS (LAST 24H)") }

            if (usageList.isEmpty()) {
                item {
                    EmptyState(
                        message = "No microphone access recorded in the last 24 hours",
                        icon = Icons.Filled.MicOff
                    )
                }
            } else {
                items(usageList, key = { it.id }) { usage ->
                    MicUsageItem(usage = usage, isAlert = alertApps.any { it.id == usage.id })
                }
            }
        }
    }
}

@Composable
fun MicUsageItem(usage: AppMicUsage, isAlert: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isAlert) AccentRed.copy(0.15f) else AccentCyan.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    tint = if (isAlert) AccentRed else AccentCyan,
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
                    val mins = TimeUnit.MILLISECONDS.toMinutes(usage.durationMs)
                    Text("${mins}m", style = MaterialTheme.typography.bodySmall, color = if (isAlert) AccentRed else TextSecondary)
                }
            }
            if (isAlert) PgBadge("ALERT", AccentRed)
        }
    }
}

@Composable
fun MicBarChart(usageList: List<AppMicUsage>) {
    // Build hourly buckets 0..23
    val hourly = IntArray(24)
    usageList.forEach { u ->
        val cal = Calendar.getInstance().apply { timeInMillis = u.lastAccessTime }
        hourly[cal.get(Calendar.HOUR_OF_DAY)]++
    }

    val entries = hourly.mapIndexed { h, count -> BarEntry(h.toFloat(), count.toFloat()) }
    val cyanArgb = AccentCyan.toArgb()
    val bgArgb = SurfaceCard.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard),
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setBackgroundColor(bgArgb)
                setNoDataTextColor(AndroidColor.GRAY)
                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    gridColor = AndroidColor.DKGRAY
                    axisLineColor = AndroidColor.TRANSPARENT
                    granularity = 1f
                }
                axisRight.isEnabled = false
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = AndroidColor.GRAY
                    gridColor = AndroidColor.TRANSPARENT
                    axisLineColor = AndroidColor.TRANSPARENT
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(
                        (0..23).map { if (it % 6 == 0) "${it}h" else "" }.toTypedArray()
                    )
                }
                animateY(600)
            }
        },
        update = { chart ->
            val dataSet = BarDataSet(entries, "Mic accesses").apply {
                color = cyanArgb
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 8f
                setDrawValues(false)
            }
            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}
