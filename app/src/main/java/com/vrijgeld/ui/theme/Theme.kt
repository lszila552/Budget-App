package com.vrijgeld.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary             = md_primary,
    onPrimary           = md_onPrimary,
    primaryContainer    = md_primaryContainer,
    onPrimaryContainer  = md_onPrimaryContainer,
    background          = md_background,
    onBackground        = md_onBackground,
    surface             = md_surface,
    onSurface           = md_onSurface,
    surfaceVariant      = md_surfaceVariant,
    onSurfaceVariant    = md_onSurfaceVariant,
    outline             = md_outline,
)

@Composable
fun VrijGeldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = AppTypography,
        content     = content
    )
}
