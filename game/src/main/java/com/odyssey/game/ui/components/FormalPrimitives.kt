package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.Obsidian
import com.odyssey.game.ui.theme.Stone
import com.odyssey.game.ui.theme.StonePanel
import androidx.compose.foundation.shape.RoundedCornerShape

// A clean rectangular plaque: solid fill, thin bronze rule border, no hand-drawn wobble.
fun Modifier.formalPanel(
    fill: Color = StonePanel,
    border: Color = Bronze,
    cornerRadius: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
): Modifier {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    return this
        .background(fill, shape)
        .border(borderWidth, border.copy(alpha = 0.7f), shape)
}

@Composable
fun MarbleBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Obsidian)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val laneWidth = size.width / 9f
            for (i in 0..9) {
                val x = laneWidth * i
                drawLine(
                    color = Stone.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
        }
        content()
    }
}

// A thin bronze rule with a small diamond at the center — a formal section break.
@Composable
fun OrnateDivider(modifier: Modifier = Modifier, color: Color = Bronze) {
    Canvas(modifier = modifier.fillMaxWidth().height(10.dp)) {
        val midY = size.height / 2f
        drawLine(color = color.copy(alpha = 0.6f), start = Offset(0f, midY), end = Offset(size.width, midY), strokeWidth = 1.5f)
        val cx = size.width / 2f
        val r = size.height / 2.4f
        val diamond = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, midY - r)
            lineTo(cx + r, midY)
            lineTo(cx, midY + r)
            lineTo(cx - r, midY)
            close()
        }
        drawPath(diamond, color = color, style = Stroke(width = 1.5f))
    }
}
