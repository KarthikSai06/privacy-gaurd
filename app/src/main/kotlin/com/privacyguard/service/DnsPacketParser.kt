package com.privacyguard.service

import java.nio.ByteBuffer

/**
 * Lightweight DNS packet parser for extracting query names from raw UDP/DNS packets.
 * Parses the DNS question section from a standard DNS query.
 */
object DnsPacketParser {

    data class DnsQuery(
        val transactionId: Int,
        val queryName: String,
        val queryType: Int
    )

    /**
     * Parses a DNS query from a raw UDP payload (after IP+UDP headers have been stripped).
     * Returns null if the packet is not a valid DNS query.
     */
    fun parseDnsQuery(dnsPayload: ByteArray): DnsQuery? {
        if (dnsPayload.size < 12) return null // DNS header is 12 bytes minimum

        val buffer = ByteBuffer.wrap(dnsPayload)
        val transactionId = buffer.short.toInt() and 0xFFFF
        val flags = buffer.short.toInt() and 0xFFFF

        // Check QR bit = 0 (query, not response)
        if ((flags and 0x8000) != 0) return null

        val qdCount = buffer.short.toInt() and 0xFFFF
        // Skip AN, NS, AR counts
        buffer.short // anCount
        buffer.short // nsCount
        buffer.short // arCount

        if (qdCount < 1) return null

        // Parse question name
        val name = parseDomainName(buffer) ?: return null
        if (buffer.remaining() < 4) return null

        val queryType = buffer.short.toInt() and 0xFFFF
        // queryClass = buffer.short (skip)

        return DnsQuery(transactionId, name, queryType)
    }

    /**
     * Parses an IP packet to extract the DNS query.
     * Handles IPv4 packets with UDP protocol containing DNS on port 53.
     */
    fun parseIpPacket(packet: ByteArray, length: Int): DnsQuery? {
        if (length < 20) return null

        val version = (packet[0].toInt() shr 4) and 0xF
        if (version != 4) return null // Only IPv4

        val ihl = (packet[0].toInt() and 0xF) * 4
        if (length < ihl + 8) return null

        val protocol = packet[9].toInt() and 0xFF
        if (protocol != 17) return null // Only UDP

        // UDP header starts at ihl
        val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
        if (dstPort != 53) return null // Only DNS

        val udpHeaderLen = 8
        val dnsOffset = ihl + udpHeaderLen
        if (length <= dnsOffset) return null

        val dnsPayload = packet.copyOfRange(dnsOffset, length)
        return parseDnsQuery(dnsPayload)
    }

    private fun parseDomainName(buffer: ByteBuffer): String? {
        val parts = mutableListOf<String>()
        while (buffer.hasRemaining()) {
            val len = buffer.get().toInt() and 0xFF
            if (len == 0) break
            if (len > 63) return null // Compression or invalid
            if (buffer.remaining() < len) return null
            val label = ByteArray(len)
            buffer.get(label)
            parts.add(String(label, Charsets.US_ASCII))
        }
        return if (parts.isNotEmpty()) parts.joinToString(".") else null
    }
}
