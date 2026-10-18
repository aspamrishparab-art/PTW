package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0A192F),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SafetyOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5D2C00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFF81C784),
    error = Color(0xFFEF5350),
    background = DarkNavy,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = Color(0xFF90A4AE)
)

private val LightColorScheme = lightColorScheme(
    primary = SteelNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EDF8),
    onPrimaryContainer = SteelNavy,
    secondary = SafetyOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFFE65100),
    tertiary = ComplianceGreen,
    error = AlertRed,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
