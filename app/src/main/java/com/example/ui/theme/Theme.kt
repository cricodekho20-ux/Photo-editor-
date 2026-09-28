package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioDarkSurfaceVariant,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = StudioSecondary,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = StudioTertiary,
    onTertiary = Color.White,
    background = StudioDarkBackground,
    onBackground = StudioTextLight,
    surface = StudioDarkSurface,
    onSurface = StudioTextLight,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = StudioTextMuted,
    outline = StudioDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = StudioPrimaryVariant,
    secondary = StudioSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = StudioTertiary,
    onTertiary = Color.White,
    background = StudioLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = StudioLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = StudioLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = StudioLightBorder
)

@Composable
fun PixCraftTheme(
    darkTheme: Boolean = true, // Default to sleek dark photo studio theme
    dynamicColor: Boolean = false, // Keep consistent pro photo editor dark palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    PixCraftTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
