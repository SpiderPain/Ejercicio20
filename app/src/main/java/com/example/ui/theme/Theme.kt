package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighContrastLightScheme = lightColorScheme(
    primary = HighContrastBlue,
    onPrimary = Color.White,
    primaryContainer = HighContrastBlueContainer,
    onPrimaryContainer = HighContrastBlueOnContainer,
    secondary = HighContrastGreen,
    onSecondary = Color.White,
    secondaryContainer = HighContrastGreenContainer,
    onSecondaryContainer = HighContrastGreenOnContainer,
    background = SunlightBackground,
    onBackground = SunlightBlack,
    surface = SunlightWhite,
    onSurface = SunlightBlack,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = SunlightDarkGrey,
    outline = BorderHighContrast,
    error = HighContrastRed,
    onError = Color.White,
    errorContainer = HighContrastRedContainer,
    onErrorContainer = HighContrastRedOnContainer
)

private val HighContrastDarkScheme = darkColorScheme(
    primary = Color(0xFF6EA8FE),
    onPrimary = Color(0xFF032860),
    primaryContainer = Color(0xFF084298),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF75B798),
    onSecondary = Color(0xFF0F5132),
    secondaryContainer = Color(0xFF146C43),
    onSecondaryContainer = Color.White,
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFF0F6FC),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFFC9D1D9),
    outline = Color(0xFF8B949E),
    error = Color(0xFFEA868F),
    onError = Color(0xFF58151C),
    errorContainer = Color(0xFF842029),
    onErrorContainer = Color.White
)

@Composable
fun MotContTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HighContrastDarkScheme else HighContrastLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
