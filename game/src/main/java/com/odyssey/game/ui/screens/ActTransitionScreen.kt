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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.data.SceneType
import com.odyssey.game.ui.components.FormalButton
import com.odyssey.game.ui.components.MarbleBackground
import com.odyssey.game.ui.components.OrnateDivider
import com.odyssey.game.ui.components.SceneBanner
import com.odyssey.game.ui.components.formalPanel
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TextSecondary

@Composable
fun ActTransitionScreen(onContinue: () -> Unit) {
    MarbleBackground {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .formalPanel(cornerRadius = 6.dp)
            ) {
                SceneBanner(type = SceneType.ASSEMBLY, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = "ACT TWO",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = "The Reckoning",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            OrnateDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = Bronze)
            Text(
                text = "The voyage is over. The suitors are dead. What remains is harder than either " +
                    "war or sea: holding together a kingdom you took back by force.",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            FormalButton(
                label = "Take the Throne",
                onClick = onContinue,
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }
}
