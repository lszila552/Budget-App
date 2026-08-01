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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.CharacterProp
import com.odyssey.game.ui.theme.Bronze

// A simple silhouette bust with a small corner emblem standing in for a character's prop.
@Composable
fun PortraitBust(
    accent: Color,
    prop: CharacterProp = CharacterProp.NONE,
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

        drawProp(prop, w, h)
    }
}

private fun DrawScope.drawProp(prop: CharacterProp, w: Float, h: Float) {
    val ex = w * 0.86f
    val ey = h * 0.86f
    val s = w * 0.16f
    when (prop) {
        CharacterProp.NONE -> Unit
        CharacterProp.LOOM -> {
            val path = Path().apply {
                moveTo(ex - s, ey - s); lineTo(ex - s, ey + s)
                moveTo(ex, ey - s); lineTo(ex, ey + s)
                moveTo(ex + s, ey - s); lineTo(ex + s, ey + s)
                moveTo(ex - s, ey - s); lineTo(ex + s, ey - s)
            }
            drawPath(path, color = Bronze, style = Stroke(width = 1.6f))
        }
        CharacterProp.SPEAR -> {
            drawLine(color = Bronze, start = Offset(ex - s, ey + s), end = Offset(ex + s, ey - s), strokeWidth = 2.2f)
            val tip = Path().apply {
                moveTo(ex + s, ey - s)
                lineTo(ex + s * 0.6f, ey - s * 0.4f)
                lineTo(ex + s * 1.1f, ey - s * 0.6f)
                close()
            }
            drawPath(tip, color = Bronze)
        }
        CharacterProp.CROOK -> {
            val path = Path().apply {
                moveTo(ex, ey + s)
                lineTo(ex, ey - s * 0.4f)
                quadraticBezierTo(ex, ey - s, ex - s * 0.7f, ey - s * 0.7f)
                quadraticBezierTo(ex - s * 1.3f, ey - s * 0.4f, ex - s * 0.7f, ey - s * 0.1f)
            }
            drawPath(path, color = Bronze, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
        CharacterProp.OWL -> {
            drawCircle(color = Bronze, radius = s * 0.7f, center = Offset(ex, ey))
            drawCircle(color = Color.Black, radius = s * 0.18f, center = Offset(ex - s * 0.28f, ey - s * 0.1f))
            drawCircle(color = Color.Black, radius = s * 0.18f, center = Offset(ex + s * 0.28f, ey - s * 0.1f))
            val ears = Path().apply {
                moveTo(ex - s * 0.5f, ey - s * 0.6f); lineTo(ex - s * 0.2f, ey - s); lineTo(ex - s * 0.1f, ey - s * 0.5f)
                moveTo(ex + s * 0.5f, ey - s * 0.6f); lineTo(ex + s * 0.2f, ey - s); lineTo(ex + s * 0.1f, ey - s * 0.5f)
            }
            drawPath(ears, color = Bronze)
        }
        CharacterProp.RAISED_ARM -> {
            drawLine(color = Bronze, start = Offset(ex, ey + s), end = Offset(ex, ey - s), strokeWidth = 3f, cap = StrokeCap.Round)
            drawCircle(color = Bronze, radius = s * 0.32f, center = Offset(ex, ey - s))
        }
    }
}
