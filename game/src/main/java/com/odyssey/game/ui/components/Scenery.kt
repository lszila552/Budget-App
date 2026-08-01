package com.odyssey.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.odyssey.game.data.SceneType
import kotlin.random.Random

// Layered procedural backdrops: gradient sky, a sea or floor line, and per-location silhouette shapes.
@Composable
fun SceneBanner(type: SceneType, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (type) {
            SceneType.OPEN_SEA -> openSea()
            SceneType.BURNING_CITY -> burningCity()
            SceneType.COASTAL_RAID -> coastalRaid()
            SceneType.NIGHT_VISION -> nightVision()
            SceneType.TROPICAL_SHORE -> tropicalShore()
            SceneType.CAVE_MOUTH -> caveMouth()
            SceneType.WINDY_ISLE -> windyIsle()
            SceneType.CLIFF_HARBOR -> cliffHarbor()
            SceneType.ENCHANTED_HALL -> enchantedHall()
            SceneType.UNDERWORLD -> underworld()
            SceneType.ROCKY_SIRENS -> rockySirens()
            SceneType.WHIRLPOOL -> whirlpool()
            SceneType.SACRED_PASTURE -> sacredPasture()
            SceneType.ISLAND_GROTTO -> islandGrotto()
            SceneType.HARBOR_CITY -> harborCity()
            SceneType.PALACE_HALL -> palaceHall()
            SceneType.ASSEMBLY -> assembly()
            SceneType.PALACE_INTERIOR -> palaceInterior()
            SceneType.RAMPARTS_NIGHT -> rampartsNight()
            SceneType.GATES_BATTLE -> gatesBattle()
        }
    }
}

// ---- shared primitives ----

private fun DrawScope.sky(top: Color, bottom: Color) {
    drawRect(brush = Brush.verticalGradient(listOf(top, bottom)), size = size)
}

private fun DrawScope.horizonSea(color: Color, horizonFrac: Float = 0.62f) {
    val y = size.height * horizonFrac
    drawRect(color = color, topLeft = Offset(0f, y), size = Size(size.width, size.height - y))
    val crest = Path().apply {
        moveTo(0f, y)
        val segs = 10
        for (i in 1..segs) {
            val x = size.width * i / segs
            lineTo(x, y + if (i % 2 == 0) -3f else 3f)
        }
    }
    drawPath(crest, color = color.copy(alpha = 0.7f), style = Stroke(width = 2f))
}

private fun DrawScope.stars(seed: Int, count: Int, color: Color, maxYFrac: Float = 0.55f) {
    val rnd = Random(seed)
    repeat(count) {
        drawCircle(
            color = color,
            radius = rnd.nextFloat() * 1.1f + 0.4f,
            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height * maxYFrac)
        )
    }
}

private fun DrawScope.moon(color: Color) {
    drawCircle(color = color, radius = size.height * 0.09f, center = Offset(size.width * 0.82f, size.height * 0.2f))
}

private fun DrawScope.sun(color: Color, cxFrac: Float = 0.78f, cyFrac: Float = 0.22f) {
    drawCircle(color = color, radius = size.height * 0.11f, center = Offset(size.width * cxFrac, size.height * cyFrac))
}

private fun DrawScope.islandBump(cxFrac: Float, widthFrac: Float, color: Color, horizonFrac: Float = 0.62f) {
    val baseY = size.height * horizonFrac
    val w = size.width * widthFrac
    val cx = size.width * cxFrac
    val path = Path().apply {
        moveTo(cx - w / 2f, baseY)
        quadraticBezierTo(cx, baseY - w * 0.45f, cx + w / 2f, baseY)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.shipSilhouette(cxFrac: Float, color: Color, horizonFrac: Float = 0.62f) {
    val baseY = size.height * horizonFrac
    val cx = size.width * cxFrac
    val w = size.width * 0.1f
    val hull = Path().apply {
        moveTo(cx - w, baseY)
        lineTo(cx + w, baseY)
        lineTo(cx + w * 0.7f, baseY + w * 0.35f)
        lineTo(cx - w * 0.7f, baseY + w * 0.35f)
        close()
    }
    drawPath(hull, color = color)
    drawLine(color = color, start = Offset(cx, baseY), end = Offset(cx, baseY - w * 1.6f), strokeWidth = 2f)
    val sail = Path().apply {
        moveTo(cx, baseY - w * 1.5f)
        lineTo(cx, baseY - w * 0.3f)
        lineTo(cx + w * 0.9f, baseY - w * 0.6f)
        close()
    }
    drawPath(sail, color = color.copy(alpha = 0.9f))
}

private fun DrawScope.columns(count: Int, color: Color, topYFrac: Float = 0.2f, baseYFrac: Float = 0.88f) {
    val topY = size.height * topYFrac
    val baseY = size.height * baseYFrac
    val colW = size.width / (count * 2.2f)
    for (i in 0 until count) {
        val cx = size.width * (i + 0.5f) / count
        drawRect(color = color, topLeft = Offset(cx - colW / 2f, topY), size = Size(colW, baseY - topY))
        drawRect(color = color, topLeft = Offset(cx - colW * 0.8f, topY - colW * 0.3f), size = Size(colW * 1.6f, colW * 0.3f))
    }
    drawRect(color = color, topLeft = Offset(0f, baseY), size = Size(size.width, size.height * 0.03f))
}

private fun DrawScope.crowdSilhouette(count: Int, color: Color, baseYFrac: Float = 0.86f, seed: Int = 5) {
    val rnd = Random(seed)
    val baseY = size.height * baseYFrac
    val h = size.height * 0.16f
    repeat(count) { i ->
        val x = size.width * (i + 0.5f) / count + (rnd.nextFloat() - 0.5f) * (size.width / count) * 0.6f
        drawCircle(color = color, radius = h * 0.26f, center = Offset(x, baseY - h))
        drawRoundRect(
            color = color,
            topLeft = Offset(x - h * 0.2f, baseY - h * 0.72f),
            size = Size(h * 0.4f, h * 0.72f),
            cornerRadius = CornerRadius(h * 0.12f)
        )
    }
}

private fun DrawScope.torch(cxFrac: Float, color: Color, flameColor: Color) {
    val cx = size.width * cxFrac
    val baseY = size.height * 0.88f
    drawLine(color = color, start = Offset(cx, baseY), end = Offset(cx, baseY - size.height * 0.4f), strokeWidth = 4f)
    drawCircle(color = flameColor, radius = size.height * 0.045f, center = Offset(cx, baseY - size.height * 0.44f))
}

private fun DrawScope.birds(seed: Int, count: Int, color: Color, maxYFrac: Float = 0.35f) {
    val rnd = Random(seed)
    repeat(count) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height * maxYFrac
        val w = size.width * 0.03f
        val path = Path().apply {
            moveTo(x - w, y)
            quadraticBezierTo(x, y - w * 0.6f, x + w, y)
        }
        drawPath(path, color = color, style = Stroke(width = 1.6f, cap = StrokeCap.Round))
    }
}

// ---- scenes ----

private fun DrawScope.openSea() {
    sky(Color(0xFF4E7FA8), Color(0xFF9FC3D8))
    sun(Color(0xFFF0DFA0))
    horizonSea(Color(0xFF2E5876))
    shipSilhouette(0.28f, Color(0xFF16232E))
    islandBump(0.85f, 0.14f, Color(0xFF2E5876).copy(alpha = 0.5f))
}

private fun DrawScope.burningCity() {
    sky(Color(0xFF3A1B14), Color(0xFF8C3A22))
    horizonSea(Color(0xFF241210), 0.7f)
    columns(4, Color(0xFF1A0D0A), topYFrac = 0.35f, baseYFrac = 0.7f)
    repeat(3) { i ->
        val x = size.width * (0.2f + i * 0.3f)
        drawPath(
            Path().apply {
                moveTo(x, size.height * 0.7f)
                quadraticBezierTo(x - 8f, size.height * 0.45f, x, size.height * 0.2f)
                quadraticBezierTo(x + 10f, size.height * 0.5f, x, size.height * 0.7f)
            },
            color = Color(0xFFE0762E).copy(alpha = 0.85f)
        )
    }
}

private fun DrawScope.coastalRaid() {
    sky(Color(0xFF5A3B26), Color(0xFFB98A4F))
    horizonSea(Color(0xFF3E2A1C), 0.68f)
    repeat(3) { i ->
        val x = size.width * (0.25f + i * 0.25f)
        drawRect(color = Color(0xFF2A1C12), topLeft = Offset(x, size.height * 0.5f), size = Size(size.width * 0.14f, size.height * 0.18f))
        val roof = Path().apply {
            moveTo(x - 6f, size.height * 0.5f)
            lineTo(x + size.width * 0.07f, size.height * 0.38f)
            lineTo(x + size.width * 0.14f + 6f, size.height * 0.5f)
            close()
        }
        drawPath(roof, color = Color(0xFF2A1C12))
    }
    shipSilhouette(0.85f, Color(0xFF1E140D), horizonFrac = 0.68f)
}

private fun DrawScope.nightVision() {
    sky(Color(0xFF161029), Color(0xFF2F2856))
    stars(11, 40, Color(0xFFE9E0CB))
    moon(Color(0xFFE9E0CB))
    val loom = Path().apply {
        val cx = size.width * 0.5f
        val topY = size.height * 0.35f
        val baseY = size.height * 0.85f
        moveTo(cx - size.width * 0.16f, topY); lineTo(cx - size.width * 0.16f, baseY)
        moveTo(cx + size.width * 0.16f, topY); lineTo(cx + size.width * 0.16f, baseY)
        moveTo(cx - size.width * 0.16f, topY); lineTo(cx + size.width * 0.16f, topY)
        for (i in 0..6) {
            val x = cx - size.width * 0.16f + size.width * 0.32f * i / 6
            moveTo(x, topY); lineTo(x, baseY)
        }
    }
    drawPath(loom, color = Color(0xFFE9E0CB).copy(alpha = 0.25f), style = Stroke(width = 1.2f))
}

private fun DrawScope.tropicalShore() {
    sky(Color(0xFF5FA1B0), Color(0xFFCDE3C8))
    sun(Color(0xFFF3E3A0))
    horizonSea(Color(0xFF3E8E86), 0.66f)
    islandBump(0.5f, 0.9f, Color(0xFFD8C482).copy(alpha = 0.9f), horizonFrac = 0.8f)
    repeat(3) { i ->
        val x = size.width * (0.25f + i * 0.25f)
        val baseY = size.height * 0.66f
        drawLine(color = Color(0xFF2E4A2A), start = Offset(x, baseY), end = Offset(x, baseY - size.height * 0.3f), strokeWidth = 3f)
        for (a in -1..1) {
            drawLine(
                color = Color(0xFF2E4A2A),
                start = Offset(x, baseY - size.height * 0.3f),
                end = Offset(x + a * size.width * 0.05f, baseY - size.height * 0.42f),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.caveMouth() {
    sky(Color(0xFF23201B), Color(0xFF4A4136))
    val arch = Path().apply {
        moveTo(size.width * 0.2f, size.height)
        lineTo(size.width * 0.2f, size.height * 0.5f)
        quadraticBezierTo(size.width * 0.5f, size.height * 0.1f, size.width * 0.8f, size.height * 0.5f)
        lineTo(size.width * 0.8f, size.height)
        close()
    }
    drawPath(arch, color = Color(0xFF0D0B09))
    drawCircle(color = Color(0xFFE0A23A), radius = size.height * 0.05f, center = Offset(size.width * 0.5f, size.height * 0.45f))
}

private fun DrawScope.windyIsle() {
    sky(Color(0xFF7FA6C4), Color(0xFFCADCE6))
    horizonSea(Color(0xFF4D7B99), 0.7f)
    islandBump(0.5f, 0.5f, Color(0xFFB7B79A), horizonFrac = 0.7f)
    repeat(3) { i ->
        val cy = size.height * (0.2f + i * 0.15f)
        val path = Path().apply {
            moveTo(size.width * 0.1f, cy)
            quadraticBezierTo(size.width * 0.3f, cy - 10f, size.width * 0.5f, cy)
            quadraticBezierTo(size.width * 0.7f, cy + 10f, size.width * 0.9f, cy)
        }
        drawPath(path, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.cliffHarbor() {
    sky(Color(0xFF4A5A63), Color(0xFF8FA3AC))
    horizonSea(Color(0xFF2E3E45), 0.72f)
    val left = Path().apply {
        moveTo(0f, size.height); lineTo(0f, size.height * 0.15f)
        lineTo(size.width * 0.22f, size.height * 0.4f); lineTo(size.width * 0.18f, size.height)
        close()
    }
    val right = Path().apply {
        moveTo(size.width, size.height); lineTo(size.width, size.height * 0.1f)
        lineTo(size.width * 0.78f, size.height * 0.35f); lineTo(size.width * 0.82f, size.height)
        close()
    }
    drawPath(left, color = Color(0xFF232B2F))
    drawPath(right, color = Color(0xFF232B2F))
    shipSilhouette(0.5f, Color(0xFF0F1416), horizonFrac = 0.72f)
}

private fun DrawScope.enchantedHall() {
    sky(Color(0xFF33224A), Color(0xFF6B4A8C))
    columns(5, Color(0xFF20142F))
    drawCircle(
        color = Color(0xFFC99AE0).copy(alpha = 0.35f),
        radius = size.height * 0.22f,
        center = Offset(size.width * 0.5f, size.height * 0.5f)
    )
}

private fun DrawScope.underworld() {
    sky(Color(0xFF0D0D10), Color(0xFF2A2632))
    stars(23, 14, Color(0xFF8C86A0), maxYFrac = 0.4f)
    repeat(5) { i ->
        val x = size.width * (0.1f + i * 0.2f)
        val h = size.height * (0.3f + (i % 3) * 0.08f)
        drawRoundRect(
            color = Color(0xFF1A1720),
            topLeft = Offset(x, size.height - h),
            size = Size(size.width * 0.05f, h),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

private fun DrawScope.rockySirens() {
    sky(Color(0xFF6E8FA0), Color(0xFFB9CFD6))
    horizonSea(Color(0xFF3C5F70), 0.68f)
    repeat(3) { i ->
        val x = size.width * (0.2f + i * 0.3f)
        val rock = Path().apply {
            moveTo(x - size.width * 0.06f, size.height)
            lineTo(x, size.height * 0.45f)
            lineTo(x + size.width * 0.07f, size.height)
            close()
        }
        drawPath(rock, color = Color(0xFF33474E))
    }
    birds(31, 4, Color(0xFF1D2B30), maxYFrac = 0.4f)
}

private fun DrawScope.whirlpool() {
    sky(Color(0xFF223244), Color(0xFF3E5A6E))
    horizonSea(Color(0xFF15222C), 0.4f)
    val cx = size.width * 0.62f
    val cy = size.height * 0.72f
    var r = size.height * 0.32f
    repeat(4) {
        drawCircle(color = Color(0xFF0B1319).copy(alpha = 0.7f), radius = r, center = Offset(cx, cy), style = Stroke(width = 3f))
        r *= 0.68f
    }
    val cliff = Path().apply {
        moveTo(0f, size.height); lineTo(0f, size.height * 0.2f)
        lineTo(size.width * 0.18f, size.height * 0.5f); lineTo(size.width * 0.12f, size.height)
        close()
    }
    drawPath(cliff, color = Color(0xFF1A2731))
}

private fun DrawScope.sacredPasture() {
    sky(Color(0xFF9FC2D6), Color(0xFFE9DDB0))
    sun(Color(0xFFF6E27A), cyFrac = 0.18f)
    drawRect(color = Color(0xFF8FA05C), topLeft = Offset(0f, size.height * 0.7f), size = Size(size.width, size.height * 0.3f))
    repeat(3) { i ->
        val x = size.width * (0.2f + i * 0.3f)
        val baseY = size.height * 0.78f
        drawRoundRect(color = Color(0xFFEDE7DA), topLeft = Offset(x, baseY), size = Size(size.width * 0.12f, size.height * 0.08f), cornerRadius = CornerRadius(6f))
        drawCircle(color = Color(0xFFEDE7DA), radius = size.height * 0.05f, center = Offset(x - 4f, baseY))
    }
}

private fun DrawScope.islandGrotto() {
    sky(Color(0xFF6FA8AE), Color(0xFFD9E7CE))
    horizonSea(Color(0xFF3E8A80), 0.66f)
    islandBump(0.5f, 0.95f, Color(0xFF6E9C5A).copy(alpha = 0.85f), horizonFrac = 0.78f)
    val arch = Path().apply {
        moveTo(size.width * 0.38f, size.height * 0.78f)
        lineTo(size.width * 0.38f, size.height * 0.55f)
        quadraticBezierTo(size.width * 0.5f, size.height * 0.42f, size.width * 0.62f, size.height * 0.55f)
        lineTo(size.width * 0.62f, size.height * 0.78f)
        close()
    }
    drawPath(arch, color = Color(0xFF2E4A2A))
}

private fun DrawScope.harborCity() {
    sky(Color(0xFF6E92B0), Color(0xFFD8E2E8))
    horizonSea(Color(0xFF3E6A8C), 0.7f)
    repeat(5) { i ->
        val x = size.width * i / 5f
        val h = size.height * (0.12f + (i % 3) * 0.06f)
        drawRect(color = Color(0xFF334860), topLeft = Offset(x, size.height * 0.7f - h), size = Size(size.width / 5.2f, h))
    }
    shipSilhouette(0.15f, Color(0xFF16232E), horizonFrac = 0.7f)
}

private fun DrawScope.palaceHall() {
    sky(Color(0xFF3A2A1C), Color(0xFF8C6A3E))
    columns(5, Color(0xFF241A10))
    drawRect(color = Color(0xFF6B4A22).copy(alpha = 0.5f), topLeft = Offset(0f, size.height * 0.85f), size = Size(size.width, size.height * 0.15f))
}

private fun DrawScope.assembly() {
    sky(Color(0xFF5A4A33), Color(0xFFA8905E))
    drawRect(color = Color(0xFF8C7748), topLeft = Offset(0f, size.height * 0.8f), size = Size(size.width, size.height * 0.2f))
    crowdSilhouette(7, Color(0xFF2A2013))
}

private fun DrawScope.palaceInterior() {
    sky(Color(0xFF2E2418), Color(0xFF5E4A2E))
    columns(3, Color(0xFF1E160E), topYFrac = 0.15f)
    torch(0.12f, Color(0xFF3A2C1A), Color(0xFFE0A23A))
    torch(0.88f, Color(0xFF3A2C1A), Color(0xFFE0A23A))
}

private fun DrawScope.rampartsNight() {
    sky(Color(0xFF131426), Color(0xFF272B4A))
    stars(41, 30, Color(0xFFE9E0CB))
    moon(Color(0xFFE9E0CB))
    drawRect(color = Color(0xFF1C1A14), topLeft = Offset(0f, size.height * 0.78f), size = Size(size.width, size.height * 0.22f))
    repeat(4) { i ->
        val x = size.width * i / 4f
        drawRect(color = Color(0xFF1C1A14), topLeft = Offset(x, size.height * 0.68f), size = Size(size.width * 0.12f, size.height * 0.14f))
    }
    torch(0.5f, Color(0xFF3A2C1A), Color(0xFFE0A23A))
}

private fun DrawScope.gatesBattle() {
    sky(Color(0xFF3E1A16), Color(0xFF8C3A2E))
    drawRect(color = Color(0xFF1C1610), topLeft = Offset(0f, size.height * 0.78f), size = Size(size.width, size.height * 0.22f))
    val gateW = size.width * 0.22f
    val gateX = (size.width - gateW) / 2f
    drawRect(color = Color(0xFF120E0A), topLeft = Offset(gateX, size.height * 0.35f), size = Size(gateW, size.height * 0.43f))
    crowdSilhouette(9, Color(0xFF120E0A), baseYFrac = 0.78f, seed = 17)
    repeat(9) { i ->
        val x = size.width * (i + 0.5f) / 9
        drawLine(
            color = Color(0xFF120E0A),
            start = Offset(x, size.height * 0.62f),
            end = Offset(x + 6f, size.height * 0.42f),
            strokeWidth = 2f
        )
    }
}
