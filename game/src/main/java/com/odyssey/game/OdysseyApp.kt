package com.odyssey.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odyssey.game.state.GameViewModel
import com.odyssey.game.state.Screen
import com.odyssey.game.ui.components.MarbleBackground
import com.odyssey.game.ui.screens.ActTransitionScreen
import com.odyssey.game.ui.screens.EndingScreen
import com.odyssey.game.ui.screens.EventScreen
import com.odyssey.game.ui.screens.MainMenuScreen
import com.odyssey.game.ui.screens.MapScreen

@Composable
fun OdysseyApp(viewModel: GameViewModel = viewModel()) {
    val state = viewModel.state

    AnimatedContent(targetState = state.screen, label = "screen") { screen ->
        when (screen) {
            Screen.MENU -> MainMenuScreen(onBeginVoyage = viewModel::beginVoyage)

            Screen.MAP -> MarbleBackground {
                MapScreen(
                    itinerary = viewModel.itinerary,
                    act1Size = viewModel.act1Size,
                    stopIndex = state.stopIndex,
                    act = viewModel.act(state.stopIndex),
                    crew = state.crew,
                    supplies = state.supplies,
                    favor = state.favor,
                    stability = state.stability,
                    log = state.log,
                    onContinueJourney = viewModel::openCurrentEvent,
                )
            }

            Screen.EVENT -> MarbleBackground {
                EventScreen(
                    stop = viewModel.currentStop(),
                    act = viewModel.act(state.stopIndex),
                    crew = state.crew,
                    supplies = state.supplies,
                    favor = state.favor,
                    stability = state.stability,
                    onChoose = viewModel::chooseOption,
                )
            }

            Screen.ACT_TRANSITION -> ActTransitionScreen(onContinue = viewModel::continueFromActTransition)

            Screen.ENDING -> {
                val ending = state.ending
                if (ending != null) {
                    EndingScreen(
                        ending = ending,
                        act = viewModel.act(state.stopIndex),
                        crew = state.crew,
                        supplies = state.supplies,
                        favor = state.favor,
                        stability = state.stability,
                        onRestart = viewModel::restart,
                    )
                }
            }
        }
    }
}
