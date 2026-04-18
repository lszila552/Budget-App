package com.vrijgeld.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun darkScheme(accent: Color) = darkColorScheme(
    primary             = accent,
    onPrimary           = Color(0xFF003824),
    primaryContainer    = Color(0xFF00522F),
    onPrimaryContainer  = Color(0xFF70FFBA),
    background          = md_background,
    onBackground        = md_onBackground,
    surface             = md_surface,
    onSurface           = md_onSurface,
    surfaceVariant      = md_surfaceVariant,
    onSurfaceVariant    = md_onSurfaceVariant,
    outline             = md_outline,
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary             = accent,
    onPrimary           = Color.White,
    primaryContainer    = accent.copy(alpha = 0.15f),
    onPrimaryContainer  = accent,
    background          = LightBackground,
    onBackground        = LightTextPrimary,
    surface             = LightSurface,
    onSurface           = LightTextPrimary,
    surfaceVariant      = LightSurfaceVar,
    onSurfaceVariant    = LightTextSecondary,
    outline             = LightOutline,
)

@Composable
fun VrijGeldTheme(
    appTheme: AppTheme = AppTheme.DARK,
    accentIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val accent = ACCENT_OPTIONS.getOrNull(accentIndex) ?: Accent
    val useDark = when (appTheme) {
        AppTheme.DARK   -> true
        AppTheme.LIGHT  -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (useDark) darkScheme(accent) else lightScheme(accent)

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        content     = content
    )
}
