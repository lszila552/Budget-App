package com.odyssey.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Stop
import com.odyssey.game.state.LogEntry
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.ShipGlyph
import com.odyssey.game.ui.components.SketchButton
import com.odyssey.game.ui.components.sketchPanel
import com.odyssey.game.ui.theme.AegeanBlue
import com.odyssey.game.ui.theme.GoldOchre
import com.odyssey.game.ui.theme.Ink
import com.odyssey.game.ui.theme.ParchmentDark
import com.odyssey.game.ui.theme.ParchmentPanel

@Composable
fun MapScreen(
    itinerary: List<Stop>,
    stopIndex: Int,
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
            text = "The Voyage — Leg ${stopIndex + 1} of ${itinerary.size}",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        ResourceHud(crew = crew, supplies = supplies, favor = favor, stability = stability)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(itinerary.size) { i ->
                MapNodeRow(stop = itinerary[i], state = nodeState(i, stopIndex), showConnector = i != itinerary.lastIndex)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .sketchPanel(fill = ParchmentPanel, cornerRadius = 14.dp, seed = 5002)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Odysseus's Journal",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (log.isEmpty()) {
                    Text(
                        text = "The voyage has not yet begun. Ahead lies ${currentStop.title} — ${currentStop.subtitle}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
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
                                    color = if (entry.fateStruck) GoldOchre else AegeanBlue,
                                )
                                Text(
                                    text = entry.outcomeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink,
                                )
                            }
                        }
                    }
                }
            }
        }

        SketchButton(
            label = if (stopIndex == itinerary.lastIndex) "Reach Ithaca's Shore" else "Continue the Voyage",
            onClick = onContinueJourney,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 14.dp)
        )
    }
}

private enum class NodeState { VISITED, CURRENT, UPCOMING }

private fun nodeState(index: Int, currentIndex: Int): NodeState = when {
    index < currentIndex -> NodeState.VISITED
    index == currentIndex -> NodeState.CURRENT
    else -> NodeState.UPCOMING
}

@Composable
private fun MapNodeRow(stop: Stop, state: NodeState, showConnector: Boolean) {
    val fill = when (state) {
        NodeState.VISITED -> AegeanBlue
        NodeState.CURRENT -> GoldOchre
        NodeState.UPCOMING -> ParchmentDark
    }
    val textColor = if (state == NodeState.UPCOMING) Ink.copy(alpha = 0.5f) else Ink

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .sketchPanel(fill = fill, cornerRadius = 999.dp, seed = stop.id.hashCode()),
                contentAlignment = Alignment.Center
            ) {
                if (state == NodeState.CURRENT) {
                    ShipGlyph(modifier = Modifier.size(16.dp), color = Ink)
                }
            }
            Text(
                text = stop.title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (showConnector) {
            Canvas(modifier = Modifier.width(18.dp).height(4.dp)) {
                drawLine(
                    color = Color(0x552B2013),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 3f
                )
            }
        }
    }
}
