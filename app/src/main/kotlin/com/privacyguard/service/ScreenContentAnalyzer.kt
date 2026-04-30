package com.privacyguard.service

import android.util.Log
import java.util.regex.Pattern

/**
 * Screen Content DLP — Data Loss Prevention.
 *
 * Analyzes text visible on screen (from AccessibilityService events)
 * to detect when sensitive data (credit cards, SSNs, OTPs, passwords)
 * appears, and alerts the user.
 */
class ScreenContentAnalyzer {

    data class SensitiveDataMatch(
        val type: String,
        val masked: String,          // e.g., "**** **** **** 1234"
        val confidence: Float
    )

    private val patterns = mapOf(
        "Credit Card" to Pattern.compile("\\b(?:4\\d{3}|5[1-5]\\d{2}|3[47]\\d{2}|6(?:011|5\\d{2}))[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}\\b"),
        "OTP Code" to Pattern.compile("\\b(?:OTP|otp|code|Code|PIN|pin)[:\\s]*([0-9]{4,8})\\b"),
        "SSN" to Pattern.compile("\\b\\d{3}[-]\\d{2}[-]\\d{4}\\b"),
        "Aadhaar Number" to Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"),
        "PAN Card" to Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b"),
        "Phone Number" to Pattern.compile("\\b(?:\\+91|\\+1)?[-\\s]?\\d{10}\\b"),
        "Email Address" to Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
        "Bank Account" to Pattern.compile("\\b\\d{9,18}\\b"),  // Generic long number
        "Password Visible" to Pattern.compile("(?i)password[:\\s]+\\S{4,}")
    )

    fun analyze(text: String): List<SensitiveDataMatch> {
        val matches = mutableListOf<SensitiveDataMatch>()

        for ((type, pattern) in patterns) {
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val rawMatch = matcher.group()
                matches.add(
                    SensitiveDataMatch(
                        type = type,
                        masked = maskData(type, rawMatch),
                        confidence = getConfidence(type, rawMatch)
                    )
                )
            }
        }

        return matches
    }

    private fun maskData(type: String, raw: String): String {
        return when (type) {
            "Credit Card" -> {
                val digits = raw.replace("[\\s-]".toRegex(), "")
                "**** **** **** ${digits.takeLast(4)}"
            }
            "SSN" -> "***-**-${raw.takeLast(4)}"
            "Aadhaar Number" -> "**** **** ${raw.takeLast(4)}"
            "PAN Card" -> "${raw.take(2)}***${raw.takeLast(1)}"
            "Phone Number" -> "****${raw.takeLast(4)}"
            "Email Address" -> {
                val parts = raw.split("@")
                "${parts[0].take(2)}****@${parts.getOrElse(1) { "***" }}"
            }
            else -> "****"
        }
    }

    private fun getConfidence(type: String, raw: String): Float {
        return when (type) {
            "Credit Card" -> if (luhnCheck(raw.replace("[\\s-]".toRegex(), ""))) 0.95f else 0.4f
            "OTP Code" -> 0.8f
            "SSN" -> 0.9f
            "Aadhaar Number" -> 0.85f
            "PAN Card" -> 0.9f
            "Password Visible" -> 0.7f
            else -> 0.5f
        }
    }

    /** Luhn algorithm to validate credit card numbers */
    private fun luhnCheck(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 13 || digits.length > 19) return false

        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }
}
