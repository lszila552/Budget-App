package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Ink
import com.odyssey.game.ui.theme.Parchment
import kotlin.random.Random

/** Hand-inked panel background: a soft fill plus two overlapping wobbly border strokes. */
fun Modifier.sketchPanel(
    fill: Color,
    ink: Color = Ink,
    cornerRadius: Dp = 14.dp,
    seed: Int = 0,
    strokeWidth: Dp = 2.dp,
): Modifier = this.drawWithCache {
    val cornerPx = cornerRadius.toPx()
    val strokePx = strokeWidth.toPx()
    val paths = roughRoundedRect(size, cornerPx, seed)
    onDrawBehind {
        drawPath(paths[0], color = fill)
        drawPath(
            paths[1],
            color = ink.copy(alpha = 0.85f),
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            paths[2],
            color = ink.copy(alpha = 0.30f),
            style = Stroke(width = strokePx * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** Just the wobbly ink outline, no fill — for lighter-weight chrome like dividers/frames. */
fun Modifier.sketchOutline(
    ink: Color = Ink,
    cornerRadius: Dp = 14.dp,
    seed: Int = 0,
    strokeWidth: Dp = 1.6.dp,
): Modifier = this.drawWithCache {
    val cornerPx = cornerRadius.toPx()
    val strokePx = strokeWidth.toPx()
    val paths = roughRoundedRect(size, cornerPx, seed)
    onDrawBehind {
        drawPath(paths[1], color = ink, style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Full-screen parchment backdrop with a faint procedural speckle/fiber texture. */
@Composable
fun PaperBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Parchment)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rnd = Random(42)
            repeat(260) {
                val x = rnd.nextFloat() * size.width
                val y = rnd.nextFloat() * size.height
                val r = rnd.nextFloat() * 1.4f + 0.3f
                drawCircle(
                    color = Ink.copy(alpha = 0.035f),
                    radius = r,
                    center = Offset(x, y)
                )
            }
            repeat(18) {
                val x = rnd.nextFloat() * size.width
                val y = rnd.nextFloat() * size.height
                val len = rnd.nextFloat() * 60f + 20f
                val angle = rnd.nextFloat() * (Math.PI.toFloat() * 2f)
                drawLine(
                    color = Ink.copy(alpha = 0.02f),
                    start = Offset(x, y),
                    end = Offset(x + kotlin.math.cos(angle) * len, y + kotlin.math.sin(angle) * len),
                    strokeWidth = 1f
                )
            }
        }
        content()
    }
}

/** A short hand-drawn squiggly divider, like a scribble under a chapter heading. */
@Composable
fun WaveDivider(modifier: Modifier = Modifier, color: Color = Ink, seed: Int = 0) {
    Canvas(modifier = modifier) {
        val rnd = Random(seed)
        val path = androidx.compose.ui.graphics.Path()
        val segs = 10
        path.moveTo(0f, size.height / 2f)
        for (i in 1..segs) {
            val x = size.width * i / segs
            val y = size.height / 2f + (rnd.nextFloat() * 2f - 1f) * size.height * 0.35f
            path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
