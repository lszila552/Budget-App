package com.vrijgeld.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import com.vrijgeld.ui.theme.RedOver
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.SurfaceVar
import com.vrijgeld.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    navController: NavController,
    viewModel: WeeklyReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Weekly Review") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Budget pace card
            ReviewCard(title = "Budget Pace") {
                val expected = state.daysIntoMonth.toFloat() / 30f
                val actual   = state.budgetPacePercent
                val paceColor = when {
                    actual > expected * 1.2f -> RedOver
                    actual > expected        -> AmberWarn
                    else                     -> Accent
                }
                Text(
                    "${(actual * 100).toInt()}% spent",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = paceColor
                )
                Text(
                    "Expected ${(expected * 100).toInt()}% by day ${state.daysIntoMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Top 3 categories
            if (state.topCategories.isNotEmpty()) {
                ReviewCard(title = "Top Spending Categories") {
                    state.topCategories.forEach { cat ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(cat.icon, fontSize = 18.sp)
                                Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "€${"%.2f".format(cat.spentCents / 100.0)}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val pct = if (cat.budgetCents > 0) (cat.spentCents * 100 / cat.budgetCents).toInt() else 0
                                Text("$pct% of budget", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        HorizontalDivider(color = SurfaceColor, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            // Anomalies
            if (state.anomalies.isNotEmpty()) {
                ReviewCard(title = "Anomalies") {
                    state.anomalies.forEach { anomaly ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(anomaly.description, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Normal: €${"%.2f".format(anomaly.normalCents / 100.0)}",
                                    style = MaterialTheme.typography.labelSmall, color = TextSecondary
                                )
                            }
                            Text(
                                "€${"%.2f".format(anomaly.amountCents / 100.0)}",
                                fontFamily = JetBrainsMonoFamily, fontSize = 13.sp, color = AmberWarn
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // Savings trend
            ReviewCard(title = "Savings Rate") {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    SavingsRateColumn("This month", state.savingsRateThisMonth)
                    SavingsRateColumn("Last month", state.savingsRateLastMonth)
                }
            }

            // Reflection note
            ReviewCard(title = "Weekly Reflection") {
                OutlinedTextField(
                    value         = state.reflectionNote,
                    onValueChange = viewModel::setReflectionNote,
                    placeholder   = { Text("What went well? What to improve?", color = TextSecondary) },
                    modifier      = Modifier.fillMaxWidth().height(100.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Background,
                        unfocusedContainerColor = Background,
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReviewCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            content()
        }
    }
}

@Composable
private fun SavingsRateColumn(label: String, rate: Float) {
    val color = when {
        rate >= 0.2f -> Accent
        rate >= 0f   -> AmberWarn
        else         -> RedOver
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${(rate * 100).toInt()}%",
            fontFamily = JetBrainsMonoFamily,
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
