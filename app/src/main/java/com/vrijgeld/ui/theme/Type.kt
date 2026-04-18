package com.vrijgeld.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.vrijgeld.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val OutfitFamily = FontFamily(
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Bold),
)

val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Bold,   fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Bold,   fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Bold,   fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Medium, fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Medium, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Medium, fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Medium, fontSize = 22.sp),
    bodyLarge     = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = OutfitFamily,       fontWeight = FontWeight.Normal, fontSize = 11.sp),
)
