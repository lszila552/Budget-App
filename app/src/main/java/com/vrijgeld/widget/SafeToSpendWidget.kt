package com.vrijgeld.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.text.NumberFormat
import java.util.Locale

val KEY_DAILY_STS = longPreferencesKey("daily_sts_cents")

class SafeToSpendWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val prefs    = currentState<androidx.datastore.preferences.core.Preferences>()
        val cents    = prefs[KEY_DAILY_STS] ?: 0L
        val fmt      = NumberFormat.getCurrencyInstance(Locale("nl", "NL"))
        val display  = fmt.format(cents / 100.0)
        val color    = if (cents >= 0) Color(0xFF34D399) else Color(0xFFEF4444)

        Box(
            modifier      = GlanceModifier.fillMaxSize().background(Color(0xFF0B0D11)).padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "Today",
                    style = TextStyle(
                        color      = ColorProvider(Color(0xFF9CA3AF)),
                        fontSize   = 12.sp
                    )
                )
                Text(
                    text  = display,
                    style = TextStyle(
                        color      = ColorProvider(color),
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text  = "safe to spend",
                    style = TextStyle(
                        color    = ColorProvider(Color(0xFF9CA3AF)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
