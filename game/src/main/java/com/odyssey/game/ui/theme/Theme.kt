package com.odyssey.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OdysseyColorScheme = lightColorScheme(
    primary            = AegeanBlue,
    onPrimary          = ParchmentPanel,
    primaryContainer   = AegeanBlueLt,
    onPrimaryContainer = Ink,
    secondary          = GoldOchre,
    onSecondary        = Ink,
    background         = Parchment,
    onBackground       = Ink,
    surface            = ParchmentPanel,
    onSurface          = Ink,
    surfaceVariant     = ParchmentDark,
    onSurfaceVariant   = InkFaded,
    error              = WineRed,
    onError            = ParchmentPanel,
    outline            = Ink,
)

@Composable
fun OdysseyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OdysseyColorScheme,
        typography  = OdysseyTypography,
        content     = content
    )
}
