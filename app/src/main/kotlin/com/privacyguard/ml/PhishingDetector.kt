package com.privacyguard.ml

import android.content.Context
import java.util.regex.Pattern

/**
 * On-device phishing detector using pattern matching and heuristics.
 * Analyzes SMS messages, notification text, and clipboard content
 * for phishing indicators without requiring internet access.
 */
class PhishingDetector(private val context: Context) {

    data class PhishingResult(
        val isPhishing: Boolean,
        val riskScore: Float,        // 0.0 – 1.0
        val detectedUrl: String,
        val reasons: List<String>
    )

    private val urlPattern = Pattern.compile(
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)" +
        "|(www\\.[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)" +
        "|([\\w-]+\\.(com|net|org|info|xyz|tk|ml|ga|cf|top|buzz|click|link|work|club)(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?)"
    )

    private val urgencyKeywords = listOf(
        "urgent", "immediately", "expire", "suspended", "verify your",
        "confirm your", "update your", "click here", "act now",
        "limited time", "account locked", "unusual activity",
        "unauthorized", "security alert", "your account"
    )

    private val phishingPatterns = listOf(
        "won a prize", "you have been selected", "claim your reward",
        "free gift", "lottery winner", "cash prize", "congratulations",
        "kyc update", "pan card", "aadhar", "otp sharing",
        "credit card expired", "bank account"
    )

    private val suspiciousTlds = listOf(
        ".xyz", ".tk", ".ml", ".ga", ".cf", ".top", ".buzz",
        ".click", ".link", ".work", ".club", ".icu", ".monster"
    )

    private val impersonationDomains = listOf(
        "amaz0n", "paypa1", "micros0ft", "g00gle", "faceb00k",
        "netfl1x", "appl3", "wh4tsapp", "instagrm", "telegr4m",
        "bank-secure", "account-verify", "login-update"
    )

    fun analyze(text: String): PhishingResult {
        val lowerText = text.lowercase()
        val reasons = mutableListOf<String>()
        var score = 0f

        // 1. Extract URLs
        val matcher = urlPattern.matcher(text)
        val urls = mutableListOf<String>()
        while (matcher.find()) {
            urls.add(matcher.group())
        }
        val primaryUrl = urls.firstOrNull() ?: ""

        // 2. Check for URL shorteners
        val shorteners = listOf("bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly")
        if (urls.any { url -> shorteners.any { url.contains(it) } }) {
            score += 0.2f
            reasons.add("Contains shortened URL (hiding real destination)")
        }

        // 3. Check suspicious TLDs
        if (urls.any { url -> suspiciousTlds.any { tld -> url.contains(tld) } }) {
            score += 0.25f
            reasons.add("Uses suspicious domain extension")
        }

        // 4. Check impersonation domains
        if (urls.any { url -> impersonationDomains.any { domain -> url.lowercase().contains(domain) } }) {
            score += 0.35f
            reasons.add("URL impersonates a known brand")
        }

        // 5. Urgency language
        val urgencyCount = urgencyKeywords.count { lowerText.contains(it) }
        if (urgencyCount >= 2) {
            score += 0.15f * urgencyCount.coerceAtMost(3)
            reasons.add("Uses urgency language ($urgencyCount indicators)")
        }

        // 6. Known phishing patterns
        val patternCount = phishingPatterns.count { lowerText.contains(it) }
        if (patternCount > 0) {
            score += 0.2f * patternCount.coerceAtMost(3)
            reasons.add("Matches known phishing patterns")
        }

        // 7. Has URL + asks for credentials
        val credentialKeywords = listOf("password", "pin", "otp", "cvv", "ssn", "login", "credentials")
        if (urls.isNotEmpty() && credentialKeywords.any { lowerText.contains(it) }) {
            score += 0.3f
            reasons.add("Contains URL and asks for sensitive credentials")
        }

        // 8. IP address in URL (no domain name)
        val ipPattern = Pattern.compile("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
        if (urls.any { ipPattern.matcher(it).find() }) {
            score += 0.3f
            reasons.add("URL uses IP address instead of domain name")
        }

        score = score.coerceIn(0f, 1f)

        return PhishingResult(
            isPhishing = score >= 0.4f,
            riskScore = score,
            detectedUrl = primaryUrl,
            reasons = reasons
        )
    }
}
