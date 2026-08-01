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
import com.odyssey.game.state.Act
import com.odyssey.game.state.LogEntry
import com.odyssey.game.ui.components.CrownGlyph
import com.odyssey.game.ui.components.FormalButton
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.ShipGlyph
import com.odyssey.game.ui.components.formalPanel
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.Obsidian
import com.odyssey.game.ui.theme.StoneLight
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

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(itinerary.size) { i ->
                MapNodeRow(
                    stop = itinerary[i],
                    state = nodeState(i, stopIndex),
                    isCouncil = i >= act1Size,
                    showConnector = i != itinerary.lastIndex
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

private enum class NodeState { VISITED, CURRENT, UPCOMING }

private fun nodeState(index: Int, currentIndex: Int): NodeState = when {
    index < currentIndex -> NodeState.VISITED
    index == currentIndex -> NodeState.CURRENT
    else -> NodeState.UPCOMING
}

@Composable
private fun MapNodeRow(stop: Stop, state: NodeState, isCouncil: Boolean, showConnector: Boolean) {
    val fill = when (state) {
        NodeState.VISITED -> if (isCouncil) TyrianPurple else Bronze
        NodeState.CURRENT -> Bronze
        NodeState.UPCOMING -> StoneLight
    }
    val textColor = if (state == NodeState.UPCOMING) TextSecondary.copy(alpha = 0.6f) else TextPrimary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .formalPanel(fill = fill, cornerRadius = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state == NodeState.CURRENT) {
                    if (isCouncil) CrownGlyph(modifier = Modifier.size(16.dp), color = Obsidian)
                    else ShipGlyph(modifier = Modifier.size(16.dp), color = Obsidian)
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
                    color = Color(0x55B08D3E),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 3f
                )
            }
        }
    }
}
