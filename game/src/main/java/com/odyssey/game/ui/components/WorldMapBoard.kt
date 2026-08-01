package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Stop
import com.odyssey.game.state.Act
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.Obsidian
import com.odyssey.game.ui.theme.StoneLight
import com.odyssey.game.ui.theme.TyrianPurple
import kotlin.random.Random

private const val COLS = 4
private val ROW_HEIGHT = 92.dp
private val NODE_SIZE = 30.dp

private fun snakeColRow(index: Int): Pair<Int, Int> {
    val row = index / COLS
    val colInRow = index % COLS
    val col = if (row % 2 == 0) colInRow else COLS - 1 - colInRow
    return col to row
}

private enum class NodeState { VISITED, CURRENT, UPCOMING }

private fun nodeStateFor(index: Int, currentIndex: Int): NodeState = when {
    index < currentIndex -> NodeState.VISITED
    index == currentIndex -> NodeState.CURRENT
    else -> NodeState.UPCOMING
}

// A tilted, winding snake-grid path over a parallax backdrop — one pseudo-3D plane via a graphicsLayer tilt.
@Composable
fun WorldMapBoard(
    itinerary: List<Stop>,
    act1Size: Int,
    stopIndex: Int,
    act: Act,
    modifier: Modifier = Modifier,
) {
    val rows = (itinerary.size + COLS - 1) / COLS
    val boardHeight = ROW_HEIGHT * rows
    val scrollState = rememberScrollState()
    val localDensity = LocalDensity.current

    Box(modifier = modifier.verticalScroll(scrollState)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(boardHeight)
                .graphicsLayer {
                    rotationX = 25f
                    cameraDistance = 16f * density
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
        ) {
            val cellWidth = maxWidth / COLS

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBoardBackdrop(
                    act = act,
                    count = itinerary.size,
                    rowHeightPx = ROW_HEIGHT.toPx(),
                    cellWidthPx = cellWidth.toPx(),
                )
            }

            itinerary.forEachIndexed { i, _ ->
                val (col, row) = snakeColRow(i)
                val cx = cellWidth * (col + 0.5f) - NODE_SIZE / 2
                val cy = ROW_HEIGHT * row + ROW_HEIGHT / 2 - NODE_SIZE / 2
                val state = nodeStateFor(i, stopIndex)
                val isCouncil = i >= act1Size
                val fill = when (state) {
                    NodeState.VISITED -> if (isCouncil) TyrianPurple else Bronze
                    NodeState.CURRENT -> Bronze
                    NodeState.UPCOMING -> StoneLight
                }

                Box(
                    modifier = Modifier
                        .offset(x = cx, y = cy)
                        .size(NODE_SIZE)
                        .formalPanel(fill = fill, cornerRadius = NODE_SIZE / 2),
                    contentAlignment = Alignment.Center
                ) {
                    if (state == NodeState.CURRENT) {
                        if (isCouncil) CrownGlyph(modifier = Modifier.size(16.dp), color = Obsidian)
                        else ShipGlyph(modifier = Modifier.size(16.dp), color = Obsidian)
                    }
                }
            }
        }
    }

    LaunchedEffect(stopIndex) {
        val (_, row) = snakeColRow(stopIndex)
        val targetPx = with(localDensity) { (ROW_HEIGHT * row).toPx() } - 260f
        scrollState.animateScrollTo(targetPx.coerceAtLeast(0f).toInt())
    }
}

private fun DrawScope.drawBoardBackdrop(act: Act, count: Int, rowHeightPx: Float, cellWidthPx: Float) {
    val topColor = if (act == Act.VOYAGE) Color(0xFF23384A) else Color(0xFF2E2013)
    val bottomColor = if (act == Act.VOYAGE) Color(0xFF6E97AC) else Color(0xFF6B4A22)
    drawRect(brush = Brush.verticalGradient(listOf(topColor, bottomColor)), size = size)

    val rnd = Random(7)
    repeat(16) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val r = rnd.nextFloat() * 10f + 4f
        drawCircle(color = Color.White.copy(alpha = 0.06f), radius = r, center = Offset(x, y))
    }

    val points = (0 until count).map { i ->
        val (col, row) = snakeColRow(i)
        Offset(cellWidthPx * (col + 0.5f), rowHeightPx * row + rowHeightPx / 2f)
    }
    val path = Path()
    points.forEachIndexed { i, p ->
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    drawPath(path, color = Bronze.copy(alpha = 0.55f), style = Stroke(width = 4f, cap = StrokeCap.Round))
}
