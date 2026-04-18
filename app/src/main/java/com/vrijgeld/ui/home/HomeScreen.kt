package com.vrijgeld.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vrijgeld.ui.components.CategoryRing
import com.vrijgeld.ui.components.SafeToSpendHero
import com.vrijgeld.ui.components.SavingsArrow
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import com.vrijgeld.ui.theme.RedOver
import com.vrijgeld.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet && state.breakdown != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor   = com.vrijgeld.ui.theme.Surface
        ) {
            StsBreakdownSheet(
                breakdown  = state.breakdown!!,
                onDismiss  = { showSheet = false }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment         = Alignment.CenterVertically,
            horizontalArrangement     = Arrangement.SpaceBetween
        ) {
            SavingsArrow(rate = state.savingsRate, target = state.targetSavingsRate)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.ringCategories.forEach { CategoryRing(it) }
            }
        }

        SafeToSpendHero(
            daily    = state.safeToSpendToday,
            monthly  = state.safeToSpendMonth,
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { showSheet = true }
        )
    }
}

@Composable
private fun StsBreakdownSheet(breakdown: StsBreakdown, onDismiss: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Safe-to-spend breakdown",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider()

        BreakdownRow("Income this month",     breakdown.incomeThisMonth,     positive = true)
        BreakdownRow("Fixed bills remaining", breakdown.fixedRemaining,      positive = false)
        BreakdownRow("Savings allocations",   breakdown.savingsAllocations,  positive = false)
        BreakdownRow("Spent so far",          breakdown.variableSpent,       positive = false)

        HorizontalDivider()

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("= Safe to spend", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                "€${"%.2f".format(breakdown.safeToSpend / 100.0)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = if (breakdown.safeToSpend >= 0) Accent else RedOver
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Close")
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amountCents: Long, positive: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        val sign = if (positive) "+" else "−"
        Text(
            "$sign€${"%.2f".format(amountCents / 100.0)}",
            fontFamily = JetBrainsMonoFamily,
            fontSize   = 14.sp,
            color      = if (positive) Accent else MaterialTheme.colorScheme.onSurface
        )
    }
}
