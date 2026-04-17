package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.ui.components.BudgetProgressBar
import com.vrijgeld.ui.navigation.Screen
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs        = listOf("Budget", "Subscriptions", "Forecast")

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Budget") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) { popUpTo(0) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).background(Background)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Surface
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        text     = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> BudgetTab(state.items)
                1 -> SubscriptionsTab(
                    subscriptions       = state.subscriptions,
                    totalMonthlySubCost = state.totalMonthlySubCost,
                    onConfirm           = viewModel::confirm,
                    onDismiss           = viewModel::dismiss
                )
                2 -> ForecastTab(state.forecast)
            }
        }
    }
}

@Composable
private fun BudgetTab(items: List<CategorySpendingItem>) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items, key = { it.category.id }) { item ->
            if (item.category.monthlyBudget != null) {
                BudgetProgressBar(
                    label  = item.category.name,
                    icon   = item.category.icon,
                    spent  = item.spent,
                    budget = item.category.monthlyBudget
                )
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.category.icon} ${item.category.name}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("€${item.spent / 100}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

