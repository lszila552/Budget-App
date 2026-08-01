package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Ending
import com.odyssey.game.data.EndingKind
import com.odyssey.game.ui.components.PaperBackground
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.SketchButton
import com.odyssey.game.ui.components.WaveDivider
import com.odyssey.game.ui.components.sketchPanel
import com.odyssey.game.ui.theme.AegeanBlue
import com.odyssey.game.ui.theme.Ink
import com.odyssey.game.ui.theme.ParchmentPanel
import com.odyssey.game.ui.theme.StabilityColor

@Composable
fun EndingScreen(
    ending: Ending,
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    onRestart: () -> Unit,
) {
    val accent = if (ending.kind == EndingKind.VICTORY) AegeanBlue else StabilityColor

    PaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (ending.kind) {
                    EndingKind.VICTORY -> "ITHACA RECLAIMED"
                    EndingKind.LOST_AT_SEA -> "THE VOYAGE ENDS"
                    EndingKind.THRONE_LOST -> "THE THRONE FALLS"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = ending.title,
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            WaveDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), color = accent)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .sketchPanel(fill = ParchmentPanel, cornerRadius = 14.dp, seed = 9001)
                    .padding(16.dp)
            ) {
                Text(
                    text = ending.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                )
            }

            Text(
                text = "The kingdom, as it stands:",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            ResourceHud(crew = crew, supplies = supplies, favor = favor, stability = stability)

            SketchButton(
                label = "Sail Again",
                onClick = onRestart,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
