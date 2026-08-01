package com.odyssey.game.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.Outline
import kotlin.random.Random

// Sample a clean path, jitter the points with a seeded Random, rebuild a smooth path through them.
private fun smoothClosedPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    val n = points.size
    fun mid(a: Offset, b: Offset) = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)

    path.moveTo(mid(points[n - 1], points[0]).x, mid(points[n - 1], points[0]).y)
    for (i in 0 until n) {
        val current = points[i]
        val next = points[(i + 1) % n]
        val m = mid(current, next)
        path.quadraticBezierTo(current.x, current.y, m.x, m.y)
    }
    path.close()
    return path
}

private fun jitteredPointsAlong(base: Path, size: Size, pointCount: Int, jitter: Float, seed: Int): List<Offset> {
    val measure = PathMeasure()
    measure.setPath(base, false)
    val length = measure.length
    if (length <= 0f) return listOf(Offset(0f, 0f), Offset(size.width, 0f), Offset(size.width, size.height), Offset(0f, size.height))
    val rnd = Random(seed)
    return (0 until pointCount).map { i ->
        val d = length * i / pointCount
        val p = measure.getPosition(d)
        Offset(
            p.x + (rnd.nextFloat() * 2f - 1f) * jitter,
            p.y + (rnd.nextFloat() * 2f - 1f) * jitter
        )
    }
}

// Returns [fill, inkPass1, inkPass2]: a barely-jittered fill silhouette, then two more-jittered ink passes.
fun roughRoundedRect(size: Size, cornerRadiusPx: Float, seed: Int, jitter: Float = 3f, pointCount: Int = 40): List<Path> {
    val clampedCorner = cornerRadiusPx.coerceIn(0f, minOf(size.width, size.height) / 2f)
    val base = Path().apply {
        addOutline(Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, CornerRadius(clampedCorner))))
    }
    val fillPoints = jitteredPointsAlong(base, size, pointCount, jitter * 0.35f, seed)
    val ink1Points = jitteredPointsAlong(base, size, pointCount, jitter, seed + 101)
    val ink2Points = jitteredPointsAlong(base, size, pointCount, jitter, seed + 907)
    return listOf(smoothClosedPath(fillPoints), smoothClosedPath(ink1Points), smoothClosedPath(ink2Points))
}

/** A single hand-drawn stroke between two points, gently bowed rather than perfectly straight. */
fun sketchLine(start: Offset, end: Offset, seed: Int, waviness: Float = 4f): Path {
    val rnd = Random(seed)
    val path = Path()
    path.moveTo(start.x, start.y)
    val steps = 5
    for (i in 1..steps) {
        val t = i / steps.toFloat()
        val x = start.x + (end.x - start.x) * t
        val y = start.y + (end.y - start.y) * t
        val perpX = -(end.y - start.y)
        val perpY = (end.x - start.x)
        val perpLen = kotlin.math.hypot(perpX, perpY).takeIf { it > 0f } ?: 1f
        val wobble = (rnd.nextFloat() * 2f - 1f) * waviness
        path.lineTo(x + perpX / perpLen * wobble, y + perpY / perpLen * wobble)
    }
    return path
}
