package com.privacyguard.domain

data class PrivacyScoreBreakdown(
    val score: Int,            // 0-100
    val keyloggerPenalty: Int,
    val nightPenalty: Int,
    val cameraPenalty: Int,
    val locationPenalty: Int,
    val micPenalty: Int,
    val triggerPenalty: Int,
    val label: String          // "Good" | "Fair" | "At Risk"
)

object PrivacyScoreCalculator {

    fun calculate(
        suspiciousKeyloggers: Int,
        nightEvents: Int,
        cameraAccesses: Int,
        locationAccesses: Int,
        micAccesses: Int,
        triggerPairs: Int
    ): PrivacyScoreBreakdown {
        val kl  = minOf(suspiciousKeyloggers * 20, 40)
        val ngt = minOf(nightEvents * 5, 20)
        val cam = minOf(cameraAccesses * 5, 15)
        val loc = minOf(locationAccesses * 3, 12)
        val mic = minOf(micAccesses * 2, 10)
        val trg = minOf(triggerPairs * 3, 10)

        val total = maxOf(0, 100 - kl - ngt - cam - loc - mic - trg)
        val label = when {
            total >= 80 -> "Good"
            total >= 50 -> "Fair"
            else        -> "At Risk"
        }

        return PrivacyScoreBreakdown(
            score            = total,
            keyloggerPenalty = kl,
            nightPenalty     = ngt,
            cameraPenalty    = cam,
            locationPenalty  = loc,
            micPenalty       = mic,
            triggerPenalty   = trg,
            label            = label
        )
    }
}
