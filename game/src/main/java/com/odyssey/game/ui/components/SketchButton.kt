package com.odyssey.game.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.Ink
import com.odyssey.game.ui.theme.ParchmentPanel

// A wobbly ink-rectangle button with a slight rotation, seeded by [label] so it stays put across recompositions.
@Composable
fun SketchButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color = ParchmentPanel,
    ink: Color = Ink,
    enabled: Boolean = true,
) {
    val seed = remember(label) { label.hashCode() }
    val tilt = remember(seed) { ((seed % 5) - 2) * 0.5f }
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .wrapContentSize()
            .rotate(tilt)
            .sketchPanel(fill = if (enabled) fill else fill.copy(alpha = 0.5f), ink = ink, cornerRadius = 10.dp, seed = seed)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) ink else ink.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
