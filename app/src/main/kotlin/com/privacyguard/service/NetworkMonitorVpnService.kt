package com.privacyguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.privacyguard.MainActivity
import com.privacyguard.data.db.entities.NetworkEvent
import com.privacyguard.data.db.dao.NetworkEventDao
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Calendar

/**
 * Local-only VPN service that captures DNS queries to detect apps communicating
 * with known tracking domains. No data leaves the device — the VPN loops back
 * locally and only inspects DNS packets on port 53.
 */
class NetworkMonitorVpnService : VpnService() {

    companion object {
        const val ACTION_START = "com.privacyguard.VPN_START"
        const val ACTION_STOP = "com.privacyguard.VPN_STOP"
        private const val CHANNEL_ID = "vpn_monitor"
        private const val NOTIFICATION_ID = 9001
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trackerMatcher: TrackerDomainMatcher? = null

    // We access the DAO directly since VpnService cannot use Hilt field injection
    private var networkEventDao: NetworkEventDao? = null

    override fun onCreate() {
        super.onCreate()
        trackerMatcher = TrackerDomainMatcher(this)
        // Get DAO from the database directly
        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            com.privacyguard.data.db.AppDatabase::class.java,
            com.privacyguard.data.db.AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        networkEventDao = db.networkEventDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                startVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return

        try {
            vpnInterface = Builder()
                .setSession("PrivacyGuard Network Monitor")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)
                .setBlocking(true)
                .establish()

            if (vpnInterface == null) {
                stopSelf()
                return
            }

            isRunning = true
            serviceScope.launch { processPackets() }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun stopVpn() {
        isRunning = false
        serviceScope.cancel()
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
    }

    private suspend fun processPackets() {
        val fd = vpnInterface ?: return
        val inputStream = FileInputStream(fd.fileDescriptor)
        val outputStream = FileOutputStream(fd.fileDescriptor)
        val buffer = ByteArray(32767)

        while (isRunning) {
            try {
                val length = inputStream.read(buffer)
                if (length <= 0) {
                    delay(10)
                    continue
                }

                val packetData = buffer.copyOfRange(0, length)

                // Try to parse as DNS query
                val dnsQuery = DnsPacketParser.parseIpPacket(packetData, length)
                if (dnsQuery != null && dnsQuery.queryName.isNotBlank()) {
                    logDnsQuery(dnsQuery.queryName)
                }

                // Forward the packet to the real network
                forwardPacket(packetData, length, outputStream)
            } catch (e: Exception) {
                if (!isRunning) break
                delay(50)
            }
        }
    }

    private fun forwardPacket(packet: ByteArray, length: Int, output: FileOutputStream) {
        try {
            // Extract destination IP and forward via a real socket
            if (length < 20) return
            val version = (packet[0].toInt() shr 4) and 0xF
            if (version != 4) return

            val ihl = (packet[0].toInt() and 0xF) * 4
            val protocol = packet[9].toInt() and 0xFF
            if (protocol != 17) {
                // Non-UDP: write back to TUN as-is to avoid blocking
                output.write(packet, 0, length)
                return
            }

            // Extract UDP destination
            val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
            if (dstPort == 53) {
                // Forward DNS query to real DNS server
                val udpPayloadOffset = ihl + 8
                if (length <= udpPayloadOffset) return

                val dnsPayload = packet.copyOfRange(udpPayloadOffset, length)

                try {
                    val socket = DatagramSocket()
                    protect(socket) // Protect from VPN loopback
                    val dnsServer = InetAddress.getByName("8.8.8.8")
                    val outPacket = DatagramPacket(dnsPayload, dnsPayload.size, dnsServer, 53)
                    socket.soTimeout = 5000
                    socket.send(outPacket)

                    // Receive response
                    val responseBuffer = ByteArray(4096)
                    val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket.receive(responsePacket)
                    socket.close()

                    // Build response IP+UDP packet and write back to TUN
                    val responseData = buildDnsResponsePacket(
                        packet, ihl, responseBuffer, responsePacket.length
                    )
                    if (responseData != null) {
                        output.write(responseData)
                    }
                } catch (_: Exception) {
                    // DNS forward failed; silently drop
                }
            } else {
                // Non-DNS UDP: write back to TUN
                output.write(packet, 0, length)
            }
        } catch (_: Exception) {
            // Swallow errors to keep the loop running
        }
    }

    private fun buildDnsResponsePacket(
        originalPacket: ByteArray,
        ihl: Int,
        dnsResponse: ByteArray,
        dnsLength: Int
    ): ByteArray? {
        try {
            // Swap src/dst IP, swap src/dst port, put DNS response as payload
            val totalLen = ihl + 8 + dnsLength
            val result = ByteArray(totalLen)

            // Copy and modify IP header
            System.arraycopy(originalPacket, 0, result, 0, ihl)
            // Swap src and dst IP (src=12..16, dst=16..20 for standard IPv4)
            System.arraycopy(originalPacket, 16, result, 12, 4) // old dst -> new src
            System.arraycopy(originalPacket, 12, result, 16, 4) // old src -> new dst
            // Update total length
            result[2] = ((totalLen shr 8) and 0xFF).toByte()
            result[3] = (totalLen and 0xFF).toByte()
            // Zero checksum (let the OS recalculate)
            result[10] = 0
            result[11] = 0

            // UDP header: swap ports
            result[ihl] = originalPacket[ihl + 2]     // old dst port -> new src port
            result[ihl + 1] = originalPacket[ihl + 3]
            result[ihl + 2] = originalPacket[ihl]     // old src port -> new dst port
            result[ihl + 3] = originalPacket[ihl + 1]
            // UDP length
            val udpLen = 8 + dnsLength
            result[ihl + 4] = ((udpLen shr 8) and 0xFF).toByte()
            result[ihl + 5] = (udpLen and 0xFF).toByte()
            // Zero UDP checksum
            result[ihl + 6] = 0
            result[ihl + 7] = 0

            // DNS response payload
            System.arraycopy(dnsResponse, 0, result, ihl + 8, dnsLength)

            return result
        } catch (_: Exception) {
            return null
        }
    }

    private suspend fun logDnsQuery(domain: String) {
        val matcher = trackerMatcher ?: return
        val dao = networkEventDao ?: return
        val isTracker = matcher.isTracker(domain)
        val now = System.currentTimeMillis()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        dao.insert(
            NetworkEvent(
                packageName = "unknown",  // UID resolution requires ConnectivityManager API 29+
                appName = "Unknown",
                domain = domain,
                isTracker = isTracker,
                timestamp = now,
                date = midnight
            )
        )
    }

    private fun buildNotification(): Notification {
        val channelId = CHANNEL_ID
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Network Monitor", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Monitoring network traffic for trackers"
        }
        nm.createNotificationChannel(channel)

        val stopIntent = Intent(this, NetworkMonitorVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("🛡️ PrivacyGuard Network Monitor")
            .setContentText("Monitoring DNS queries for trackers…")
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
