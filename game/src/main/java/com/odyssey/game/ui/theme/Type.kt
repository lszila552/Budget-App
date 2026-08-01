package com.odyssey.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.odyssey.game.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

// Classical inscriptional serif — titles, buttons, stat labels: a council chamber, not a storybook.
val CinzelFamily = FontFamily(
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = provider, weight = FontWeight.Bold),
)

// Old-book serif — narrative and dialogue body text.
val ManuscriptFamily = FontFamily(
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Bold),
)

val OdysseyTypography = Typography(
    displayLarge  = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Bold,   fontSize = 52.sp, letterSpacing = 1.sp),
    displayMedium = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Bold,   fontSize = 40.sp, letterSpacing = 1.sp),
    displaySmall  = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Bold,   fontSize = 30.sp),
    headlineLarge = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Medium, fontSize = 28.sp),
    headlineMedium= TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Medium, fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Bold,   fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = ManuscriptFamily,  fontWeight = FontWeight.Bold,   fontSize = 18.sp),
    bodyLarge     = TextStyle(fontFamily = ManuscriptFamily,  fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium    = TextStyle(fontFamily = ManuscriptFamily,  fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge    = TextStyle(fontFamily = CinzelFamily,      fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.5.sp),
    labelMedium   = TextStyle(fontFamily = ManuscriptFamily,  fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall    = TextStyle(fontFamily = ManuscriptFamily,  fontWeight = FontWeight.Normal, fontSize = 11.sp),
)
