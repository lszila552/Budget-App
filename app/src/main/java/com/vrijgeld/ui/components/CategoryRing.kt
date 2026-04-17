package com.vrijgeld.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.RedOver

data class CategoryRingData(
    val name: String,
    val icon: String,
    val spent: Long,
    val budget: Long
)

@Composable
fun CategoryRing(data: CategoryRingData, modifier: Modifier = Modifier) {
    val progress = if (data.budget > 0) (data.spent.toFloat() / data.budget.toFloat()).coerceIn(0f, 1.2f) else 0f
    val color = when {
        progress >= 1f   -> RedOver
        progress >= 0.9f -> AmberWarn
        else             -> Accent
    }

    Box(modifier = modifier.size(52.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val r      = (size.minDimension - stroke) / 2f
            val c      = Offset(size.width / 2f, size.height / 2f)
            val tl     = Offset(c.x - r, c.y - r)
            val sz     = Size(r * 2, r * 2)
            val style  = Stroke(width = stroke, cap = StrokeCap.Round)

            drawArc(color = color.copy(alpha = 0.2f), startAngle = -90f, sweepAngle = 360f,
                useCenter = false, style = style, topLeft = tl, size = sz)
            drawArc(color = color, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false, style = style, topLeft = tl, size = sz)
        }
        Text(text = data.icon, fontSize = 20.sp)
    }
}
