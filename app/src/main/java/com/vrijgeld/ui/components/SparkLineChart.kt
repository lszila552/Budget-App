package com.vrijgeld.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vrijgeld.ui.theme.Accent

@Composable
fun SparkLineChart(
    data: List<Pair<Int, Long>>,    // (dayOfMonth, centsSpent)
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        if (data.size < 2) return@Canvas
        val maxY  = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
        val minX  = data.minOf { it.first }.toFloat()
        val maxX  = data.maxOf { it.first }.toFloat().coerceAtLeast(minX + 1f)

        val path = Path()
        data.forEachIndexed { i, (day, amount) ->
            val x = (day - minX) / (maxX - minX) * size.width
            val y = size.height - (amount.toFloat() / maxY) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path  = path,
            color = Accent,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SimpleBarChart(
    bars: List<Pair<String, Long>>,   // (label, cents)
    maxValue: Long,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Accent.copy(alpha = 0.4f),
        Accent.copy(alpha = 0.7f),
        Accent
    )
    Canvas(modifier) {
        if (bars.isEmpty()) return@Canvas
        val barCount   = bars.size
        val totalWidth = size.width
        val barWidth   = totalWidth / (barCount * 2f)
        val maxVal     = maxValue.toFloat().coerceAtLeast(1f)

        bars.forEachIndexed { i, (_, value) ->
            val barHeight = (value.toFloat() / maxVal) * size.height
            val x         = i * (totalWidth / barCount) + barWidth / 2f
            val color     = colors.getOrElse(i) { Accent }

            drawRect(
                color   = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size    = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}
