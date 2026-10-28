package com.reelrecall.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ActiveBlue,
    secondary = ElectricIndigo,
    tertiary = ObsidianPill,
    background = DarkNeumorphic.background,
    surface = DarkNeumorphic.surface,
    surfaceVariant = DarkNeumorphic.surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkNeumorphic.textPrimary,
    onSurface = DarkNeumorphic.textPrimary,
    onSurfaceVariant = DarkNeumorphic.textSecondary,
    outline = DarkNeumorphic.border
)

private val LightColorScheme = lightColorScheme(
    primary = LightNeumorphic.accent,
    secondary = ElectricIndigo,
    tertiary = LightNeumorphic.pillBg,
    background = LightNeumorphic.background,
    surface = LightNeumorphic.surface,
    surfaceVariant = LightNeumorphic.surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightNeumorphic.textPrimary,
    onSurface = LightNeumorphic.textPrimary,
    onSurfaceVariant = LightNeumorphic.textSecondary,
    outline = LightNeumorphic.border
)

@Composable
fun ReelRecallTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgColor = if (isDarkTheme) DarkNeumorphic.background else LightNeumorphic.background
            window.statusBarColor = bgColor.toArgb()
            window.navigationBarColor = bgColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
