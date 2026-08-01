package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Choice
import com.odyssey.game.data.Stop
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.SketchButton
import com.odyssey.game.ui.components.WaveDivider
import com.odyssey.game.ui.components.sketchPanel
import com.odyssey.game.ui.theme.AegeanBlue
import com.odyssey.game.ui.theme.Ink
import com.odyssey.game.ui.theme.ParchmentPanel

@Composable
fun EventScreen(
    stop: Stop,
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    onChoose: (Choice) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ResourceHud(crew = crew, supplies = supplies, favor = favor, stability = stability)

        Text(
            text = if (stop.isInterlude) stop.subtitle else stop.title,
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            text = if (stop.isInterlude) "— a vision from Ithaca —" else stop.subtitle,
            style = MaterialTheme.typography.titleMedium,
            fontStyle = FontStyle.Italic,
            color = AegeanBlue,
        )
        WaveDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = AegeanBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sketchPanel(fill = ParchmentPanel, cornerRadius = 14.dp, seed = stop.id.hashCode() + 3)
                .padding(14.dp)
        ) {
            Text(
                text = stop.narrative,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
        }

        Text(
            text = "What do you command?",
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stop.choices.forEach { choice ->
                SketchButton(
                    label = choice.label,
                    onClick = { onChoose(choice) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
