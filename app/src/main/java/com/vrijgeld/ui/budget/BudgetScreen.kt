package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.domain.EnvelopeState
import com.vrijgeld.domain.InsightType
import com.vrijgeld.domain.SpendingInsight
import com.vrijgeld.ui.navigation.Screen
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
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs        = listOf("Budget", "Subscriptions", "Forecast")

    if (state.overspendTarget != null) {
        OverspendDialog(
            target    = state.overspendTarget!!,
            sources   = (state.regularEnvelopes + state.sinkingEnvelopes)
                .filter { !it.isOverspent && it.remaining > 0 && it.category.id != state.overspendTarget!!.category.id },
            onTransfer = { sourceId -> viewModel.transferAllocation(sourceId, state.overspendTarget!!.category.id) },
            onDismiss  = { viewModel.setOverspendTarget(null) }
        )
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).background(Background)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = SurfaceColor) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> BudgetTab(
                    state         = state,
                    onAllocate    = { navController.navigate(Screen.Allocate.route) },
                    onCategoryTap = { env ->
                        if (env.isOverspent) viewModel.setOverspendTarget(env)
                        else navController.navigate(Screen.CategoryDetail.createRoute(env.category.id))
                    }
                )
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
private fun BudgetTab(
    state: BudgetUiState,
    onAllocate: () -> Unit,
    onCategoryTap: (EnvelopeState) -> Unit
) {
    if (state.regularEnvelopes.isEmpty() && state.sinkingEnvelopes.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No budget categories yet", style = MaterialTheme.typography.titleMedium)
                Text("Add income and categories in Settings to get started.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            UnallocatedBanner(
                unallocated = state.unallocatedIncome,
                totalIncome = state.totalIncome,
                onAllocate  = onAllocate
            )
        }

        if (state.insights.isNotEmpty()) {
            item { InsightsCard(state.insights) }
        }

        if (state.regularEnvelopes.isNotEmpty()) {
            item {
                Text(
                    "Monthly Budgets",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(state.regularEnvelopes, key = { it.category.id }) { env ->
                val lastMonth = state.lastMonthSpentByCategory[env.category.id]
                EnvelopeCard(env = env, lastMonthSpent = lastMonth, onClick = { onCategoryTap(env) })
            }
        }

        if (state.sinkingEnvelopes.isNotEmpty()) {
            item {
                Text(
                    "Sinking Funds",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.sinkingEnvelopes, key = { it.category.id }) { env ->
                val lastMonth = state.lastMonthSpentByCategory[env.category.id]
                EnvelopeCard(env = env, lastMonthSpent = lastMonth, onClick = { onCategoryTap(env) })
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun InsightsCard(insights: List<SpendingInsight>) {
    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Spending Insights", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            insights.forEach { insight ->
                val color = when (insight.type) {
                    InsightType.LIFESTYLE_INFLATION -> AmberWarn
                    InsightType.SMALL_PURCHASES     -> AmberWarn
                    InsightType.MERCHANT_SPIKE      -> RedOver
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚠", color = color)
                    Column {
                        Text(insight.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
                        Text(insight.body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnallocatedBanner(unallocated: Long, totalIncome: Long, onAllocate: () -> Unit) {
    val color = when {
        unallocated == 0L -> Accent
        unallocated > 0L  -> AmberWarn
        else              -> RedOver
    }
    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Income", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(
                    "€${"%.2f".format(totalIncome / 100.0)}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("Unallocated", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(
                    "€${"%.2f".format(unallocated / 100.0)}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 18.sp,
                    color      = color
                )
            }
            if (unallocated > 0L) {
                Button(
                    onClick = onAllocate,
                    colors  = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Allocate", color = androidx.compose.ui.graphics.Color.Black)
                }
            } else if (unallocated == 0L) {
                Text("✓ Zero-based", style = MaterialTheme.typography.labelSmall, color = Accent)
            }
        }
    }
}

@Composable
private fun EnvelopeCard(env: EnvelopeState, lastMonthSpent: Long?, onClick: () -> Unit) {
    val barColor = when {
        env.isOverspent          -> RedOver
        env.percentUsed >= 0.85f -> AmberWarn
        else                     -> Accent
    }

    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(env.category.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(env.category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // vs last month arrow
                    if (lastMonthSpent != null && lastMonthSpent > 0) {
                        val diff = env.spent - lastMonthSpent
                        val (icon, tint) = when {
                            diff > 0  -> Icons.Filled.ArrowUpward to RedOver
                            diff < 0  -> Icons.Filled.ArrowDownward to Accent
                            else      -> Icons.Filled.Remove to TextSecondary
                        }
                        Icon(icon, contentDescription = "vs last month", tint = tint, modifier = Modifier.size(14.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "€${"%.2f".format(env.remaining / 100.0)}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = if (env.isOverspent) RedOver else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "of €${"%.2f".format(env.available / 100.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { env.percentUsed.coerceIn(0f, 1f) },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = barColor,
                trackColor = SurfaceColor
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Spent €${"%.2f".format(env.spent / 100.0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                if (env.isOverspent) {
                    Text(
                        "⚠ Over by €${"%.2f".format(env.overspendAmount / 100.0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RedOver
                    )
                } else if (lastMonthSpent != null && lastMonthSpent > 0) {
                    val diff = env.spent - lastMonthSpent
                    val sign = if (diff >= 0) "+" else "−"
                    val absDiff = "%.2f".format(kotlin.math.abs(diff) / 100.0)
                    Text(
                        "${sign}€${absDiff} vs last month",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diff > 0) AmberWarn else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OverspendDialog(
    target: EnvelopeState,
    sources: List<EnvelopeState>,
    onTransfer: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceVar,
        title = {
            Text(
                "${target.category.icon} ${target.category.name} is €${"%.2f".format(target.overspendAmount / 100.0)} over",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    "Move money from:",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                sources.forEach { src ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onTransfer(src.category.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(src.category.icon, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(src.category.name, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "€${"%.2f".format(src.remaining / 100.0)} left",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize   = 13.sp,
                            color      = Accent
                        )
                    }
                    HorizontalDivider(color = SurfaceColor)
                }
                if (sources.isEmpty()) {
                    Text(
                        "No categories with remaining budget.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
