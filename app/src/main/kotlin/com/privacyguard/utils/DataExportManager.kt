package com.privacyguard.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.privacyguard.data.db.dao.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    private val micDao: MicUsageDao,
    private val cameraDao: CameraUsageDao,
    private val locationDao: LocationUsageDao,
    private val accessibilityDao: AccessibilityDao,
    private val nightDao: NightActivityDao,
    private val triggerDao: TriggerPairDao,
    private val networkDao: NetworkEventDao,
    private val privacyEventDao: PrivacyEventDao
) {

    suspend fun exportToZip(context: Context): File = withContext(Dispatchers.IO) {
        val dir = File(context.getExternalFilesDir(null), "exports").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val zipFile = File(dir, "PrivacyGuard_Export_$timestamp.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Mic usage
            val micData = micDao.getAllUsage().firstOrNull() ?: emptyList()
            writeCsv(zos, "mic_usage.csv", "id,packageName,appName,lastAccessTime,durationMs,date") { sb ->
                micData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${it.lastAccessTime},${it.durationMs},${it.date}") }
            }

            // Camera usage
            val camData = cameraDao.getAllUsage().firstOrNull() ?: emptyList()
            writeCsv(zos, "camera_usage.csv", "id,packageName,appName,lastAccessTime,durationMs,date") { sb ->
                camData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${it.lastAccessTime},${it.durationMs},${it.date}") }
            }

            // Location usage
            val locData = locationDao.getAllUsage().firstOrNull() ?: emptyList()
            writeCsv(zos, "location_usage.csv", "id,packageName,appName,lastAccessTime,durationMs,date") { sb ->
                locData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${it.lastAccessTime},${it.durationMs},${it.date}") }
            }

            // Accessibility records
            val accData = accessibilityDao.getAll().firstOrNull() ?: emptyList()
            writeCsv(zos, "accessibility_records.csv", "packageName,appName,serviceClass,isSuspicious,firstDetectedAt") { sb ->
                accData.forEach { sb.appendLine("${esc(it.packageName)},${esc(it.appName)},${esc(it.serviceClass)},${it.isSuspicious},${it.firstDetectedAt}") }
            }

            // Night activity
            val nightData = nightDao.getAll().firstOrNull() ?: emptyList()
            writeCsv(zos, "night_activity.csv", "id,packageName,appName,timestamp,date") { sb ->
                nightData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${it.timestamp},${it.date}") }
            }

            // Trigger pairs
            val trigData = triggerDao.getAll().firstOrNull() ?: emptyList()
            writeCsv(zos, "trigger_pairs.csv", "id,appA,appAName,appB,appBName,firstSeen,lastSeen,count") { sb ->
                trigData.forEach { sb.appendLine("${it.id},${esc(it.appA)},${esc(it.appAName)},${esc(it.appB)},${esc(it.appBName)},${it.firstSeen},${it.lastSeen},${it.count}") }
            }

            // Network events
            val netData = networkDao.getAll().firstOrNull() ?: emptyList()
            writeCsv(zos, "network_events.csv", "id,packageName,appName,domain,isTracker,timestamp,date") { sb ->
                netData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${esc(it.domain)},${it.isTracker},${it.timestamp},${it.date}") }
            }

            // Privacy events (timeline)
            val peData = privacyEventDao.getAll().firstOrNull() ?: emptyList()
            writeCsv(zos, "privacy_events.csv", "id,packageName,appName,eventType,timestamp,details") { sb ->
                peData.forEach { sb.appendLine("${it.id},${esc(it.packageName)},${esc(it.appName)},${it.eventType},${it.timestamp},${esc(it.details)}") }
            }
        }

        zipFile
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Privacy Data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun writeCsv(zos: ZipOutputStream, name: String, header: String, writer: (StringBuilder) -> Unit) {
        zos.putNextEntry(ZipEntry(name))
        val sb = StringBuilder()
        sb.appendLine(header)
        writer(sb)
        zos.write(sb.toString().toByteArray())
        zos.closeEntry()
    }

    private fun esc(s: String): String = "\"${s.replace("\"", "\"\"")}\""
}
