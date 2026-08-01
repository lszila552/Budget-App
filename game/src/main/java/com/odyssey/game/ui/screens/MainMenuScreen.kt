package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.components.PaperBackground
import com.odyssey.game.ui.components.ShipGlyph
import com.odyssey.game.ui.components.SketchButton
import com.odyssey.game.ui.components.WaveDivider
import com.odyssey.game.ui.theme.AegeanBlue
import com.odyssey.game.ui.theme.Ink

@Composable
fun MainMenuScreen(onBeginVoyage: () -> Unit) {
    PaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ShipGlyph(modifier = Modifier.height(72.dp).width(72.dp), color = AegeanBlue)

            Text(
                text = "ODYSSEY",
                style = MaterialTheme.typography.displayMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Throne of Ithaca",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center
            )

            WaveDivider(modifier = Modifier.width(200.dp).height(20.dp).padding(vertical = 12.dp), color = AegeanBlue)

            Text(
                text = "A political strategy of the long way home — steer the fleet across a " +
                    "cursed sea, and hold a kingdom together in your absence.",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            SketchButton(
                label = "Set Sail for Ithaca",
                onClick = onBeginVoyage,
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }
}
