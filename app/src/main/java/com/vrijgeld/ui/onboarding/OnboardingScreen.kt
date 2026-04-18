package com.vrijgeld.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface
import com.vrijgeld.ui.theme.TextSecondary

private data class OnboardingPage(val emoji: String, val title: String, val body: String)

private val PAGES = listOf(
    OnboardingPage(
        "💸",
        "Welcome to VrijGeld",
        "Your personal finance companion. Track spending, plan your budget, and work towards financial independence."
    ),
    OnboardingPage(
        "📥",
        "Import your transactions",
        "Export a CAMT.053 XML file from your bank and import it in Settings. VrijGeld auto-categorises your spending."
    ),
    OnboardingPage(
        "📊",
        "Zero-based budgeting",
        "Allocate every euro of income to a category. When you receive your paycheck, tap Allocate and give each euro a job."
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val current = PAGES[page]

    Box(
        modifier         = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Spacer(Modifier.weight(1f))

            Text(current.emoji, fontSize = 72.sp)

            Text(
                current.title,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Text(
                current.body,
                style     = MaterialTheme.typography.bodyMedium,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PAGES.indices.forEach { i ->
                    Box(
                        modifier = Modifier.size(if (i == page) 10.dp else 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (i == page) Accent else TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = { if (page < PAGES.lastIndex) page++ else onDone() },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(
                    if (page < PAGES.lastIndex) "Next" else "Get started",
                    color = androidx.compose.ui.graphics.Color.Black
                )
            }

            if (page < PAGES.lastIndex) {
                TextButton(onClick = onDone) {
                    Text("Skip", color = TextSecondary)
                }
            }
        }
    }
}
