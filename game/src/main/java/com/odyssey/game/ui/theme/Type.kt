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

/** Hand-lettered display face — used for titles, buttons, island names. */
val InkHandFamily = FontFamily(
    Font(googleFont = GoogleFont("Caveat"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Caveat"), fontProvider = provider, weight = FontWeight.Bold),
)

/** Old-book serif — used for narrative body text, like ink on a manuscript. */
val ManuscriptFamily = FontFamily(
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("EB Garamond"), fontProvider = provider, weight = FontWeight.Bold),
)

val OdysseyTypography = Typography(
    displayLarge  = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 60.sp),
    displayMedium = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 46.sp),
    headlineLarge = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 34.sp),
    headlineMedium= TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 22.sp),
    titleMedium   = TextStyle(fontFamily = ManuscriptFamily, fontWeight = FontWeight.Bold,   fontSize = 18.sp),
    bodyLarge     = TextStyle(fontFamily = ManuscriptFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 25.sp),
    bodyMedium    = TextStyle(fontFamily = ManuscriptFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge    = TextStyle(fontFamily = InkHandFamily,    fontWeight = FontWeight.Bold,   fontSize = 20.sp),
    labelMedium   = TextStyle(fontFamily = ManuscriptFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall    = TextStyle(fontFamily = ManuscriptFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
)
