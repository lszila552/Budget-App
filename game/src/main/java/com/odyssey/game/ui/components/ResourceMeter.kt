package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Ink
import kotlin.random.Random

// A resource bar drawn like a hand-inked meter, with a rough (not ruler-straight) fill edge.
@Composable
fun ResourceMeter(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    val seed = remember(label) { label.hashCode() }

    Column(modifier = modifier) {
        Row {
            icon()
            Text(
                text = "$label  ${value.coerceAtLeast(0)}",
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            if (fraction > 0f) {
                val fillWidth = size.width * fraction
                val fillRnd = Random(seed + 7)
                val fillPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, 0f)
                    val edgeSteps = 6
                    for (i in 0..edgeSteps) {
                        val y = size.height * i / edgeSteps
                        val wob = (fillRnd.nextFloat() * 2f - 1f) * 2f
                        lineTo((fillWidth + wob).coerceIn(0f, size.width), y)
                    }
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(fillPath, color = color.copy(alpha = 0.75f))
            }
            drawRoundRect(
                color = Ink,
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
