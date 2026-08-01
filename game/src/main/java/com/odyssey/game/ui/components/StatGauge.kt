package com.odyssey.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.StoneLight
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TextSecondary

// A clean bordered gauge: label + icon on top, a solid-fill bar below — a dashboard readout.
@Composable
fun StatGauge(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, end = 6.dp)
            )
            Text(
                text = "${value.coerceAtLeast(0)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(top = 3.dp)
                .background(StoneLight, RoundedCornerShape(2.dp))
                .border(1.dp, Bronze.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
