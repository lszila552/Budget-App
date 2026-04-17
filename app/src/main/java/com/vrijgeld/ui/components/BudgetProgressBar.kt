package com.vrijgeld.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import com.vrijgeld.ui.theme.Outline
import com.vrijgeld.ui.theme.RedOver
import com.vrijgeld.ui.theme.TextSecondary

@Composable
fun BudgetProgressBar(
    label: String,
    icon: String,
    spent: Long,
    budget: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (spent.toFloat() / budget.toFloat()).coerceIn(0f, 1f) else 0f
    val color = when {
        progress >= 1f   -> RedOver
        progress >= 0.9f -> AmberWarn
        else             -> Accent
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("$icon $label", fontSize = 14.sp, color = com.vrijgeld.ui.theme.TextPrimary)
            Text(
                "€${spent / 100} / €${budget / 100}",
                fontFamily = JetBrainsMonoFamily,
                fontSize   = 12.sp,
                color      = TextSecondary
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress           = { progress },
            modifier           = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color              = color,
            trackColor         = Outline,
        )
    }
}
