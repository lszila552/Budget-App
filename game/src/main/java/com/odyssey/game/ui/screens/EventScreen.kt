package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Choice
import com.odyssey.game.data.Stop
import com.odyssey.game.state.Act
import com.odyssey.game.ui.components.FormalChoiceButton
import com.odyssey.game.ui.components.OrnateDivider
import com.odyssey.game.ui.components.PortraitBust
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.formalPanel
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TextSecondary

@Composable
fun EventScreen(
    stop: Stop,
    act: Act,
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
        ResourceHud(act = act, crew = crew, supplies = supplies, favor = favor, stability = stability)

        val speaker = stop.speaker
        if (speaker != null) {
            Row(
                modifier = Modifier.padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PortraitBust(accent = speaker.accent)
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = speaker.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = speaker.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            Text(
                text = if (stop.isInterlude) stop.subtitle else stop.title,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = if (stop.isInterlude) "— a vision from Ithaca —" else stop.subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = TextSecondary,
            )
        }
        OrnateDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Bronze)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .formalPanel(cornerRadius = 4.dp)
                .padding(14.dp)
        ) {
            Text(
                text = stop.narrative,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
            )
        }

        Text(
            text = "What do you command?",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stop.choices.forEach { choice ->
                FormalChoiceButton(
                    label = choice.label,
                    onClick = { onChoose(choice) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
