package com.vrijgeld.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vrijgeld.ui.components.CategoryRing
import com.vrijgeld.ui.components.SafeToSpendHero
import com.vrijgeld.ui.components.SavingsArrow
import com.vrijgeld.ui.theme.Background

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        // Top row: savings arrow (left) + category rings (right)
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

        // Center: dominant safe-to-spend hero
        SafeToSpendHero(
            daily   = state.safeToSpendToday,
            monthly = state.safeToSpendMonth,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
