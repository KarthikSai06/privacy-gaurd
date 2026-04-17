package com.privacyguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = PrimaryDark,
    primaryContainer = PrimaryMid,
    onPrimaryContainer = AccentCyan,
    secondary = AccentGreen,
    onSecondary = PrimaryDark,
    tertiary = AccentAmber,
    background = PrimaryDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White,
    outline = TextMuted
)

@Composable
fun PrivacyGuardTheme(
    darkTheme: Boolean = true, // Always dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
