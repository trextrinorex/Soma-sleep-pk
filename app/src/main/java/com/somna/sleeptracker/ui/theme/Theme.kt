package com.somna.sleeptracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SomnaColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = OnIndigoPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = OnIndigoContainer,
    secondary = CelestialCyan,
    onSecondary = OnIndigoPrimary,
    secondaryContainer = CelestialCyanContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = MysticViolet,
    onTertiary = OnIndigoPrimary,
    tertiaryContainer = MysticVioletContainer,
    onTertiaryContainer = TextPrimary,
    background = MidnightDark,
    onBackground = TextPrimary,
    surface = DeepSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted
)

@Composable
fun SomnaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MidnightDark.toArgb()
            window.navigationBarColor = MidnightDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = SomnaColorScheme,
        content = content
    )
}
