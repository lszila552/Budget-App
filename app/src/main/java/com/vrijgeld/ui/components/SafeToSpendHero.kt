package com.vrijgeld.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import com.vrijgeld.ui.theme.Outline
import com.vrijgeld.ui.theme.TextSecondary

@Composable
fun SafeToSpendHero(daily: Long, monthly: Long, modifier: Modifier = Modifier) {
    val isNegativeDaily   = daily < 0
    val isNegativeMonthly = monthly < 0

    val dailyColor   = if (isNegativeDaily) AmberWarn else Accent
    val monthlyColor = if (isNegativeMonthly) AmberWarn else TextSecondary

    val dailyLabel = if (isNegativeDaily) {
        "Over by €${kotlin.math.abs(daily) / 100}"
    } else {
        "€${daily / 100}"
    }

    val monthlyLabel = if (isNegativeMonthly) {
        "Over by €${"%.2f".format(kotlin.math.abs(monthly) / 100.0)} this month"
    } else {
        "€${"%.2f".format(monthly / 100.0)} this month"
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text          = if (isNegativeDaily) "OVER BUDGET" else "SAFE TO SPEND",
            style         = MaterialTheme.typography.labelSmall,
            color         = if (isNegativeDaily) AmberWarn else TextSecondary,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = dailyLabel,
            fontFamily = JetBrainsMonoFamily,
            fontSize   = 72.sp,
            fontWeight = FontWeight.Bold,
            color      = dailyColor
        )
        Text(
            text  = if (isNegativeDaily) "today" else "today",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(20.dp))
        Surface(
            shape  = RoundedCornerShape(50),
            color  = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, Outline)
        ) {
            Text(
                text       = monthlyLabel,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                fontFamily = JetBrainsMonoFamily,
                style      = MaterialTheme.typography.bodyMedium,
                color      = monthlyColor
            )
        }
    }
}
