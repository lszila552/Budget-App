package com.odyssey.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OdysseyColorScheme = darkColorScheme(
    primary            = Bronze,
    onPrimary          = Obsidian,
    primaryContainer   = StoneLight,
    onPrimaryContainer = TextPrimary,
    secondary          = TyrianPurple,
    onSecondary        = TextPrimary,
    background         = Obsidian,
    onBackground       = TextPrimary,
    surface            = StonePanel,
    onSurface          = TextPrimary,
    surfaceVariant     = StoneLight,
    onSurfaceVariant   = TextSecondary,
    error              = BloodRed,
    onError            = TextPrimary,
    outline            = Bronze,
)

@Composable
fun OdysseyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OdysseyColorScheme,
        typography  = OdysseyTypography,
        content     = content
    )
}
