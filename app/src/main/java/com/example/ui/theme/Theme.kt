package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LuminaCyan,
    onPrimary = Color.Black,
    primaryContainer = LuminaSurfaceElevated,
    onPrimaryContainer = LuminaNeonBlue,
    secondary = LuminaNeonBlue,
    onSecondary = Color.Black,
    secondaryContainer = LuminaGlassCard,
    onSecondaryContainer = LuminaTextPrimary,
    tertiary = LuminaTeal,
    onTertiary = Color.Black,
    background = LuminaBackground,
    onBackground = LuminaTextPrimary,
    surface = LuminaSurface,
    onSurface = LuminaTextPrimary,
    surfaceVariant = LuminaSurfaceElevated,
    onSurfaceVariant = LuminaTextSecondary,
    outline = LuminaGlassBorder,
    outlineVariant = LuminaGlassHighlight,
    error = LuminaCoral,
    onError = Color.White
)

@Composable
fun LuminaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
