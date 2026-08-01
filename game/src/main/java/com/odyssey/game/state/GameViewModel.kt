package com.odyssey.game.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.odyssey.game.data.Choice
import com.odyssey.game.data.ODYSSEY_ITINERARY
import com.odyssey.game.data.Ending
import com.odyssey.game.data.Stop
import com.odyssey.game.data.crewLostEnding
import com.odyssey.game.data.resolveChoice
import com.odyssey.game.data.stabilityLostEnding
import com.odyssey.game.data.victoryEnding

enum class Screen { MENU, MAP, EVENT, ENDING }

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

    val itinerary: List<Stop> = ODYSSEY_ITINERARY

    fun currentStop(): Stop = itinerary[state.stopIndex]

    fun beginVoyage() {
        state = GameUiState(screen = Screen.MAP)
    }

    fun openCurrentEvent() {
        state = state.copy(screen = Screen.EVENT)
    }

    fun chooseOption(choice: Choice) {
        val stop = currentStop()
        val outcome = resolveChoice(choice, state.favor)

        val crew = (state.crew + outcome.effect.crew).coerceIn(0, MAX_STAT)
        val supplies = (state.supplies + outcome.effect.supplies).coerceIn(0, MAX_STAT)
        val favor = (state.favor + outcome.effect.favor).coerceIn(0, MAX_STAT)
        val stability = (state.stability + outcome.effect.stability - stop.erosion).coerceIn(0, MAX_STAT)

        val newLog = state.log + LogEntry(stop.title, choice.label, outcome.narrative, outcome.fateStruck)
        val isLastStop = state.stopIndex == itinerary.lastIndex

        val ending: Ending? = when {
            crew <= 0 -> crewLostEnding()
            stability <= 0 -> stabilityLostEnding()
            isLastStop -> victoryEnding(crew, supplies, favor, stability)
            else -> null
        }

        state = state.copy(
            crew = crew,
            supplies = supplies,
            favor = favor,
            stability = stability,
            log = newLog,
            screen = if (ending != null) Screen.ENDING else Screen.MAP,
            stopIndex = if (ending == null) state.stopIndex + 1 else state.stopIndex,
            ending = ending,
        )
    }

    fun restart() {
        state = GameUiState()
    }
}
