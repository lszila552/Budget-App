package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Bronze

// A simple silhouette bust (no portrait art available), tinted per-character, like a state seal.
@Composable
fun PortraitBust(
    accent: Color,
    modifier: Modifier = Modifier.size(72.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val medallionR = w * 0.48f

        drawCircle(color = accent.copy(alpha = 0.18f), radius = medallionR, center = Offset(cx, h / 2f))
        drawCircle(
            color = Bronze,
            radius = medallionR,
            center = Offset(cx, h / 2f),
            style = Stroke(width = 2f)
        )

        val headR = h * 0.16f
        val headCy = h * 0.36f
        drawCircle(color = accent, radius = headR, center = Offset(cx, headCy))

        val shoulders = Path().apply {
            moveTo(cx - w * 0.32f, h * 0.82f)
            quadraticBezierTo(cx - w * 0.30f, h * 0.5f, cx, h * 0.47f)
            quadraticBezierTo(cx + w * 0.30f, h * 0.5f, cx + w * 0.32f, h * 0.82f)
        }
        drawPath(
            shoulders,
            color = accent,
            style = Stroke(width = headR * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
