package com.odyssey.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.components.FormalButton
import com.odyssey.game.ui.components.MarbleBackground
import com.odyssey.game.ui.components.OrnateDivider
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.TextPrimary
import com.odyssey.game.ui.theme.TextSecondary

@Composable
fun ActTransitionScreen(onContinue: () -> Unit) {
    MarbleBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ACT TWO",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
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
