package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Stop
import com.odyssey.game.state.Act
import com.odyssey.game.state.LogEntry
import com.odyssey.game.ui.components.FormalButton
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.WorldMapBoard
import com.odyssey.game.ui.components.formalPanel
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TextSecondary
import com.odyssey.game.ui.theme.TyrianPurple

@Composable
fun MapScreen(
    itinerary: List<Stop>,
    act1Size: Int,
    stopIndex: Int,
    act: Act,
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    log: List<LogEntry>,
    onContinueJourney: () -> Unit,
) {
    val currentStop = itinerary[stopIndex]

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (act == Act.VOYAGE) "The Voyage — Leg ${stopIndex + 1} of $act1Size"
            else "The Reign — Day ${stopIndex - act1Size + 1} of ${itinerary.size - act1Size}",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        ResourceHud(act = act, crew = crew, supplies = supplies, favor = favor, stability = stability)

        WorldMapBoard(
            itinerary = itinerary,
            act1Size = act1Size,
            stopIndex = stopIndex,
            act = act,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(6.dp))
                .formalPanel(cornerRadius = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 14.dp)
                .formalPanel(cornerRadius = 4.dp)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "The Chronicle",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (log.isEmpty()) {
                    Text(
                        text = "The voyage has not yet begun. Ahead lies ${currentStop.title} — ${currentStop.subtitle}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(log.reversed()) { entry ->
                            Column {
                                Text(
                                    text = "${entry.stopTitle} — ${entry.choiceLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (entry.fateStruck) Bronze else TyrianPurple,
                                )
                                Text(
                                    text = entry.outcomeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        FormalButton(
            label = if (stopIndex == itinerary.lastIndex) "Face the Reckoning" else "Continue",
            onClick = onContinueJourney,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 14.dp)
        )
    }
}
