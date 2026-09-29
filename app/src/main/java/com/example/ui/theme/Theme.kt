package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BolotaAccentDark,
    onPrimary = DarkBg,
    primaryContainer = BolotaAccentSoftDark,
    onPrimaryContainer = BolotaAccentDark,
    secondary = BolotaPetOrange,
    onSecondary = BolotaPetInk,
    secondaryContainer = BolotaPetOrangeDark,
    onSecondaryContainer = BolotaPetOrangeLight,
    tertiary = BolotaWarnDark,
    onTertiary = DarkBg,
    error = BolotaDangerDark,
    onError = DarkBg,
    errorContainer = BolotaDangerSoftDark,
    onErrorContainer = BolotaDangerDark,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkLine
)

private val LightColorScheme = lightColorScheme(
    primary = BolotaAccent,
    onPrimary = LightSurface,
    primaryContainer = BolotaAccentSoft,
    onPrimaryContainer = BolotaAccent,
    secondary = BolotaPetOrange,
    onSecondary = LightSurface,
    secondaryContainer = BolotaPetOrangeLight,
    onSecondaryContainer = BolotaPetInk,
    tertiary = BolotaWarn,
    onTertiary = LightSurface,
    error = BolotaDanger,
    onError = LightSurface,
    errorContainer = BolotaDangerSoft,
    onErrorContainer = BolotaDanger,
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMuted,
    outline = LightLine
)

@Composable
fun BolotaTheme(
    darkTheme: Boolean = true, // Default to dark as requested in the app lore
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
