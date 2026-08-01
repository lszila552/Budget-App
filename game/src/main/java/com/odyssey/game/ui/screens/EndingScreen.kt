package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.Ending
import com.odyssey.game.data.EndingKind
import com.odyssey.game.data.SceneType
import com.odyssey.game.state.Act
import com.odyssey.game.ui.components.FormalButton
import com.odyssey.game.ui.components.MarbleBackground
import com.odyssey.game.ui.components.OrnateDivider
import com.odyssey.game.ui.components.ResourceHud
import com.odyssey.game.ui.components.SceneBanner
import com.odyssey.game.ui.components.formalPanel
import com.odyssey.game.ui.theme.BloodRed
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TyrianPurple

@Composable
fun EndingScreen(
    ending: Ending,
    act: Act,
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    onRestart: () -> Unit,
) {
    val accent = if (ending.kind == EndingKind.VICTORY) TyrianPurple else BloodRed
    val scene = when (ending.kind) {
        EndingKind.VICTORY -> SceneType.PALACE_HALL
        EndingKind.LOST_AT_SEA -> SceneType.WHIRLPOOL
        EndingKind.THRONE_LOST -> SceneType.GATES_BATTLE
        EndingKind.OVERTHROWN -> SceneType.RAMPARTS_NIGHT
        EndingKind.CIVIL_WAR -> SceneType.BURNING_CITY
    }

    MarbleBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .formalPanel(cornerRadius = 4.dp, border = accent)
            ) {
                SceneBanner(type = scene, modifier = Modifier.fillMaxSize())
            }

            Text(
                text = when (ending.kind) {
                    EndingKind.VICTORY -> "THE SAGA CONCLUDES"
                    EndingKind.LOST_AT_SEA -> "THE VOYAGE ENDS"
                    EndingKind.THRONE_LOST -> "THE THRONE FALLS"
                    EndingKind.OVERTHROWN -> "THE REIGN ENDS"
                    EndingKind.CIVIL_WAR -> "ITHACA BURNS"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = ending.title,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            OrnateDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), color = accent)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .formalPanel(cornerRadius = 4.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = ending.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                )
            }

            Text(
                text = "The kingdom, as it stands:",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            ResourceHud(act = act, crew = crew, supplies = supplies, favor = favor, stability = stability)

            FormalButton(
                label = "Sail Again",
                onClick = onRestart,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )
        }
    }
}
