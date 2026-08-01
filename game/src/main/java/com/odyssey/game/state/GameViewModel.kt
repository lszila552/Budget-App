package com.odyssey.game.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.odyssey.game.data.ACT2_ITINERARY
import com.odyssey.game.data.Choice
import com.odyssey.game.data.ODYSSEY_ITINERARY
import com.odyssey.game.data.Ending
import com.odyssey.game.data.Stop
import com.odyssey.game.data.authorityLostEnding
import com.odyssey.game.data.crewLostEnding
import com.odyssey.game.data.legacyEnding
import com.odyssey.game.data.loyaltyLostEnding
import com.odyssey.game.data.resolveChoice
import com.odyssey.game.data.stabilityLostEnding

enum class Screen { MENU, MAP, EVENT, ACT_TRANSITION, ENDING }

enum class Act { VOYAGE, COUNCIL }

data class LogEntry(val stopTitle: String, val choiceLabel: String, val outcomeText: String, val fateStruck: Boolean)

data class GameUiState(
    val screen: Screen = Screen.MENU,
    val stopIndex: Int = 0,
    val crew: Int = 100,
    val supplies: Int = 70,
    val favor: Int = 50,
    val stability: Int = 100,
    val log: List<LogEntry> = emptyList(),
    val ending: Ending? = null,
)

private const val MAX_STAT = 100

class GameViewModel : ViewModel() {

    var state by mutableStateOf(GameUiState())
        private set

    // Act 1 (the voyage) and Act 2 (governing Ithaca) form one continuous itinerary.
    val itinerary: List<Stop> = ODYSSEY_ITINERARY + ACT2_ITINERARY
    val act1Size = ODYSSEY_ITINERARY.size

    fun currentStop(): Stop = itinerary[state.stopIndex]

    fun act(stopIndex: Int = state.stopIndex): Act =
        if (stopIndex < act1Size) Act.VOYAGE else Act.COUNCIL

    fun beginVoyage() {
        state = GameUiState(screen = Screen.MAP)
    }

    fun openCurrentEvent() {
        state = state.copy(screen = Screen.EVENT)
    }

    fun continueFromActTransition() {
        state = state.copy(screen = Screen.MAP)
    }

    fun chooseOption(choice: Choice) {
        val stop = currentStop()
        val outcome = resolveChoice(choice, state.favor)
        val currentAct = act(state.stopIndex)

        val crew = (state.crew + outcome.effect.crew).coerceIn(0, MAX_STAT)
        val supplies = (state.supplies + outcome.effect.supplies).coerceIn(0, MAX_STAT)
        val favor = (state.favor + outcome.effect.favor).coerceIn(0, MAX_STAT)
        val stability = (state.stability + outcome.effect.stability - stop.erosion).coerceIn(0, MAX_STAT)

        val newLog = state.log + LogEntry(stop.title, choice.label, outcome.narrative, outcome.fateStruck)
        val wasVoyageFinale = state.stopIndex == act1Size - 1
        val isSagaFinale = state.stopIndex == itinerary.lastIndex

        val ending: Ending? = when {
            crew <= 0 -> if (currentAct == Act.VOYAGE) crewLostEnding() else loyaltyLostEnding()
            stability <= 0 -> if (currentAct == Act.VOYAGE) stabilityLostEnding() else authorityLostEnding()
            isSagaFinale -> legacyEnding(crew, supplies, favor, stability)
            else -> null
        }

        val nextScreen = when {
            ending != null -> Screen.ENDING
            wasVoyageFinale -> Screen.ACT_TRANSITION
            else -> Screen.MAP
        }

        state = state.copy(
            crew = crew,
            supplies = supplies,
            favor = favor,
            stability = stability,
            log = newLog,
            screen = nextScreen,
            stopIndex = if (ending == null) state.stopIndex + 1 else state.stopIndex,
            ending = ending,
        )
    }

    fun restart() {
        state = GameUiState()
    }
}
