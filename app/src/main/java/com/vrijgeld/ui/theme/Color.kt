package com.vrijgeld.ui.theme

import androidx.compose.ui.graphics.Color

val Background    = Color(0xFF0B0D11)
val Surface       = Color(0xFF12151C)
val SurfaceVar    = Color(0xFF1C2030)
val Accent        = Color(0xFF34D399)
val TextPrimary   = Color(0xFFEFF4F8)
val TextSecondary = Color(0xFF8A9BB0)
val Outline       = Color(0xFF2E3547)
val AmberWarn     = Color(0xFFFBBF24)
val RedOver       = Color(0xFFEF4444)

// 6 preset accent colours users can pick from
val ACCENT_OPTIONS = listOf(
    Color(0xFF34D399), // Emerald (default)
    Color(0xFF60A5FA), // Blue
    Color(0xFFA78BFA), // Violet
    Color(0xFFFBBF24), // Amber
    Color(0xFFF87171), // Rose
    Color(0xFF34D4D4), // Cyan
)
val ACCENT_NAMES = listOf("Emerald", "Blue", "Violet", "Amber", "Rose", "Cyan")

// Material3 dark scheme
val md_primary             = Accent
val md_onPrimary           = Color(0xFF003824)
val md_primaryContainer    = Color(0xFF00522F)
val md_onPrimaryContainer  = Color(0xFF70FFBA)
val md_background          = Background
val md_onBackground        = TextPrimary
val md_surface             = Surface
val md_onSurface           = TextPrimary
val md_surfaceVariant      = SurfaceVar
val md_onSurfaceVariant    = TextSecondary
val md_outline             = Outline

// Light theme colours
val LightBackground    = Color(0xFFF5F7FA)
val LightSurface       = Color(0xFFFFFFFF)
val LightSurfaceVar    = Color(0xFFEEF1F6)
val LightTextPrimary   = Color(0xFF0D1117)
val LightTextSecondary = Color(0xFF5A6A7A)
val LightOutline       = Color(0xFFD0D8E4)
