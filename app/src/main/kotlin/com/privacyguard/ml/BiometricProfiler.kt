package com.privacyguard.ml

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Behavioral Biometrics — Keystroke Dynamics Anomaly Detector.
 *
 * Builds a profile of the owner's typing behavior and detects intruders
 * using statistical distance (Mahalanobis-inspired z-score).
 */
class BiometricProfiler {

    data class TypingSession(
        val dwellTimes: List<Float>,       // ms per key press
        val flightTimes: List<Float>,      // ms between presses
        val totalDuration: Long
    )

    data class OwnerProfile(
        val meanDwell: Float,
        val stdDwell: Float,
        val meanFlight: Float,
        val stdFlight: Float,
        val meanSpeed: Float,
        val stdSpeed: Float,
        val sampleCount: Int
    )

    private var ownerProfile: OwnerProfile? = null

    fun buildProfile(
        avgDwellTimes: List<Float>,
        avgFlightTimes: List<Float>,
        avgSpeeds: List<Float>
    ) {
        if (avgDwellTimes.size < 5) return // Need at least 5 sessions

        ownerProfile = OwnerProfile(
            meanDwell = avgDwellTimes.average().toFloat(),
            stdDwell = stdDev(avgDwellTimes).coerceAtLeast(1f),
            meanFlight = avgFlightTimes.average().toFloat(),
            stdFlight = stdDev(avgFlightTimes).coerceAtLeast(1f),
            meanSpeed = avgSpeeds.average().toFloat(),
            stdSpeed = stdDev(avgSpeeds).coerceAtLeast(1f),
            sampleCount = avgDwellTimes.size
        )
    }

    /**
     * Returns an anomaly score from 0.0 (matches owner) to 1.0 (intruder).
     * Uses z-score based distance across all behavioral dimensions.
     */
    fun detectAnomaly(sessionDwell: Float, sessionFlight: Float, sessionSpeed: Float): Float {
        val profile = ownerProfile ?: return 0f // No profile yet, can't detect

        val zDwell = abs(sessionDwell - profile.meanDwell) / profile.stdDwell
        val zFlight = abs(sessionFlight - profile.meanFlight) / profile.stdFlight
        val zSpeed = abs(sessionSpeed - profile.meanSpeed) / profile.stdSpeed

        // Combined z-score (weighted)
        val combinedZ = (zDwell * 0.35f) + (zFlight * 0.35f) + (zSpeed * 0.30f)

        // Sigmoid-like mapping: z < 1.5 = normal, z > 3 = very anomalous
        return when {
            combinedZ < 1.5f -> 0f
            combinedZ < 2.0f -> 0.25f
            combinedZ < 2.5f -> 0.5f
            combinedZ < 3.0f -> 0.75f
            else -> 1.0f
        }
    }

    fun hasProfile(): Boolean = ownerProfile != null && (ownerProfile?.sampleCount ?: 0) >= 5

    private fun stdDev(values: List<Float>): Float {
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
    }
}
