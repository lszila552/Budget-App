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
import com.odyssey.game.ui.theme.Bronze

// Small line-art glyphs drawn in bronze strokes, like emblems on a formal seal.

private fun DrawScope.inkStroke(path: Path, color: Color, width: Float = 3f) {
    drawPath(path, color = color, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
fun ShipGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val hull = Path().apply {
            moveTo(w * 0.05f, h * 0.62f)
            quadraticBezierTo(w * 0.5f, h * 0.95f, w * 0.95f, h * 0.62f)
            lineTo(w * 0.8f, h * 0.62f)
            quadraticBezierTo(w * 0.5f, h * 0.78f, w * 0.2f, h * 0.62f)
            close()
        }
        val mast = Path().apply {
            moveTo(w * 0.5f, h * 0.62f)
            lineTo(w * 0.5f, h * 0.08f)
        }
        val sail = Path().apply {
            moveTo(w * 0.5f, h * 0.14f)
            quadraticBezierTo(w * 0.85f, h * 0.3f, w * 0.5f, h * 0.55f)
        }
        inkStroke(hull, color)
        inkStroke(mast, color, width = 2f)
        inkStroke(sail, color, width = 2f)
    }
}

@Composable
fun AmphoraGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.38f, h * 0.1f)
            lineTo(w * 0.62f, h * 0.1f)
            lineTo(w * 0.68f, h * 0.3f)
            quadraticBezierTo(w * 0.92f, h * 0.5f, w * 0.7f, h * 0.85f)
            quadraticBezierTo(w * 0.5f, h * 0.98f, w * 0.3f, h * 0.85f)
            quadraticBezierTo(w * 0.08f, h * 0.5f, w * 0.32f, h * 0.3f)
            close()
        }
        val handleL = Path().apply {
            moveTo(w * 0.36f, h * 0.22f)
            quadraticBezierTo(w * 0.08f, h * 0.3f, w * 0.22f, h * 0.55f)
        }
        val handleR = Path().apply {
            moveTo(w * 0.64f, h * 0.22f)
            quadraticBezierTo(w * 0.92f, h * 0.3f, w * 0.78f, h * 0.55f)
        }
        inkStroke(body, color)
        inkStroke(handleL, color, width = 2f)
        inkStroke(handleR, color, width = 2f)
    }
}

@Composable
fun LaurelGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stemL = Path().apply {
            moveTo(w * 0.5f, h * 0.95f)
            quadraticBezierTo(w * 0.15f, h * 0.7f, w * 0.3f, h * 0.1f)
        }
        val stemR = Path().apply {
            moveTo(w * 0.5f, h * 0.95f)
            quadraticBezierTo(w * 0.85f, h * 0.7f, w * 0.7f, h * 0.1f)
        }
        inkStroke(stemL, color, width = 2f)
        inkStroke(stemR, color, width = 2f)
        for (i in 0..3) {
            val t = 0.25f + i * 0.18f
            val lx = w * 0.3f + (w * 0.2f) * (1 - t)
            val ly = h * (0.75f - t * 0.6f)
            val leafL = Path().apply {
                moveTo(w * 0.3f + (0.3f - t * 0.15f) * w * 0.3f, h * (0.85f - t * 0.65f))
                quadraticBezierTo(lx - w * 0.12f, ly - h * 0.05f, lx, ly)
            }
            inkStroke(leafL, color, width = 1.6f)

            val rx = w * 0.7f - (w * 0.2f) * (1 - t)
            val leafR = Path().apply {
                moveTo(w * 0.7f - (0.3f - t * 0.15f) * w * 0.3f, h * (0.85f - t * 0.65f))
                quadraticBezierTo(rx + w * 0.12f, ly - h * 0.05f, rx, ly)
            }
            inkStroke(leafR, color, width = 1.6f)
        }
    }
}

@Composable
fun ColumnGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val capital = Path().apply {
            moveTo(w * 0.15f, h * 0.12f)
            lineTo(w * 0.85f, h * 0.12f)
        }
        val base = Path().apply {
            moveTo(w * 0.1f, h * 0.92f)
            lineTo(w * 0.9f, h * 0.92f)
        }
        val shaftL = Path().apply { moveTo(w * 0.3f, h * 0.16f); lineTo(w * 0.25f, h * 0.88f) }
        val shaftM = Path().apply { moveTo(w * 0.5f, h * 0.16f); lineTo(w * 0.5f, h * 0.88f) }
        val shaftR = Path().apply { moveTo(w * 0.7f, h * 0.16f); lineTo(w * 0.75f, h * 0.88f) }
        inkStroke(capital, color, width = 3f)
        inkStroke(base, color, width = 3f)
        inkStroke(shaftL, color, width = 2f)
        inkStroke(shaftM, color, width = 2f)
        inkStroke(shaftR, color, width = 2f)
    }
}

@Composable
fun WaveGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticBezierTo(w * 0.25f, h * 0.15f, w * 0.5f, h * 0.5f)
            quadraticBezierTo(w * 0.75f, h * 0.85f, w, h * 0.5f)
        }
        inkStroke(p, color, width = 2.2f)
    }
}

@Composable
fun CrownGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val crown = Path().apply {
            moveTo(w * 0.12f, h * 0.85f)
            lineTo(w * 0.12f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.3f)
            lineTo(w * 0.7f, h * 0.65f)
            lineTo(w * 0.88f, h * 0.5f)
            lineTo(w * 0.88f, h * 0.85f)
            close()
        }
        val base = Path().apply { moveTo(w * 0.1f, h * 0.85f); lineTo(w * 0.9f, h * 0.85f) }
        inkStroke(crown, color, width = 2f)
        inkStroke(base, color, width = 2.4f)
    }
}

@Composable
fun ScaleGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val post = Path().apply { moveTo(w * 0.5f, h * 0.08f); lineTo(w * 0.5f, h * 0.9f) }
        val beam = Path().apply { moveTo(w * 0.15f, h * 0.22f); lineTo(w * 0.85f, h * 0.22f) }
        val base = Path().apply { moveTo(w * 0.3f, h * 0.9f); lineTo(w * 0.7f, h * 0.9f) }
        val panL = Path().apply {
            moveTo(w * 0.08f, h * 0.22f)
            lineTo(w * 0.22f, h * 0.5f)
            lineTo(w * 0.06f, h * 0.5f)
            close()
        }
        val panR = Path().apply {
            moveTo(w * 0.92f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.55f)
            lineTo(w * 0.94f, h * 0.55f)
            close()
        }
        inkStroke(post, color, width = 2.2f)
        inkStroke(beam, color, width = 2f)
        inkStroke(base, color, width = 2.4f)
        inkStroke(panL, color, width = 1.6f)
        inkStroke(panR, color, width = 1.6f)
    }
}

@Composable
fun CoinGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = color, radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 2f))
        drawCircle(color = color, radius = w * 0.24f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.4f))
    }
}

@Composable
fun SwordGlyph(modifier: Modifier = Modifier.size(20.dp), color: Color = Bronze) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val blade = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            lineTo(w * 0.5f, h * 0.68f)
        }
        val guard = Path().apply { moveTo(w * 0.28f, h * 0.68f); lineTo(w * 0.72f, h * 0.68f) }
        val hilt = Path().apply { moveTo(w * 0.5f, h * 0.68f); lineTo(w * 0.5f, h * 0.92f) }
        inkStroke(blade, color, width = 3f)
        inkStroke(guard, color, width = 2.2f)
        inkStroke(hilt, color, width = 2.6f)
    }
}
