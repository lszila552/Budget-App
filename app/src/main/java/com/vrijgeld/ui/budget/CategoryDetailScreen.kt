package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vrijgeld.data.model.Transaction
import com.vrijgeld.ui.components.SimpleBarChart
import com.vrijgeld.ui.components.SparkLineChart
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.JetBrainsMonoFamily
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.SurfaceVar
import com.vrijgeld.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    navController: NavController,
    categoryId: Long,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val cat   = state.category
    val dateFmt = SimpleDateFormat("d MMM", Locale("nl"))

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(cat?.let { "${it.icon} ${it.name}" } ?: "Category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.padding(padding).background(Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily sparkline
            if (state.dailySpend.isNotEmpty()) {
                item {
                    Card(
                        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Daily spending this month",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            SparkLineChart(
                                data     = state.dailySpend,
                                modifier = Modifier.fillMaxWidth().height(60.dp)
                            )
                        }
                    }
                }
            }

            // 3-month comparison bar chart
            if (state.monthlyComparison.isNotEmpty()) {
                item {
                    Card(
                        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("3-month comparison",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            val maxVal = state.monthlyComparison.maxOfOrNull { it.second } ?: 1L
                            SimpleBarChart(
                                bars     = state.monthlyComparison,
                                maxValue = maxVal,
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                state.monthlyComparison.forEach { (ym, amount) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(ym.takeLast(2), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("€${amount / 100}", style = MaterialTheme.typography.labelSmall,
                                            fontFamily = JetBrainsMonoFamily)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transaction list header
            item {
                Text("Transactions this month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (state.transactions.isEmpty()) {
                item {
                    Text("No transactions yet.", style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary)
                }
            }

            items(state.transactions, key = { it.id }) { tx ->
                TransactionRow(tx, dateFmt)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, dateFmt: SimpleDateFormat) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(tx.description, style = MaterialTheme.typography.bodyMedium)
            Text(dateFmt.format(Date(tx.date)),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary)
        }
        Text(
            "€${kotlin.math.abs(tx.amount) / 100}",
            fontFamily = JetBrainsMonoFamily,
            fontSize   = 14.sp,
            color      = if (tx.amount < 0) MaterialTheme.colorScheme.error
                         else com.vrijgeld.ui.theme.Accent
        )
    }
}
