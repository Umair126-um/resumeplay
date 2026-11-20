package com.resumeplayer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF6C5CE7)
private val AccentVariant = Color(0xFF8E7CF0)

private val DarkColors = darkColorScheme(
    primary = AccentVariant,
    onPrimary = Color.White,
    secondary = Color(0xFF00D2A8),
    background = Color(0xFF0E0E12),
    onBackground = Color(0xFFF2F2F6),
    surface = Color(0xFF17171E),
    onSurface = Color(0xFFF2F2F6),
    surfaceVariant = Color(0xFF23232D),
    onSurfaceVariant = Color(0xFFB7B7C2)
)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Color(0xFF00B894),
    background = Color(0xFFF6F6FA),
    onBackground = Color(0xFF14141A),
    surface = Color.White,
    onSurface = Color(0xFF14141A),
    surfaceVariant = Color(0xFFECECF2),
    onSurfaceVariant = Color(0xFF5A5A66)
)

@Composable
fun ResumePlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
