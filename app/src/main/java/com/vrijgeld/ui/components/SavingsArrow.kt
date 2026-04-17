package com.vrijgeld.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import kotlin.math.roundToInt

@Composable
fun SavingsArrow(rate: Float, target: Float, modifier: Modifier = Modifier) {
    val aboveTarget = rate >= target
    val color = if (aboveTarget) Accent else AmberWarn
    val arrow = if (aboveTarget) "▲" else "▼"

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = arrow, color = color, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text       = "${(rate * 100).roundToInt()}%",
            color      = color,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize   = 14.sp
        )
    }
}
