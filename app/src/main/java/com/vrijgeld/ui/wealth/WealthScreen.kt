package com.vrijgeld.ui.wealth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.vrijgeld.data.model.Account
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.data.model.NetWorthSnapshot
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
fun WealthScreen(
    netWorthVm: NetWorthViewModel           = hiltViewModel(),
    forecastVm: CashFlowForecastViewModel   = hiltViewModel(),
    fireVm: FireViewModel                   = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Net Worth", "Forecast", "FIRE")

    Column(Modifier.fillMaxSize().background(Background)) {
        TabRow(selectedTabIndex = selectedTab, containerColor = SurfaceColor) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
            }
        }
        when (selectedTab) {
            0 -> NetWorthTab(netWorthVm)
            1 -> CashFlowForecastTab(forecastVm)
            2 -> FireTab(fireVm)
        }
    }
}

// ─── Net Worth Tab ────────────────────────────────────────────────────────────

@Composable
private fun NetWorthTab(viewModel: NetWorthViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Net worth header
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Net Worth", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        "€${"%.0f".format(state.latestNetWorth / 100.0)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.monthlyDelta != 0L) {
                        val deltaColor = if (state.monthlyDelta >= 0) Accent else RedOver
                        val sign       = if (state.monthlyDelta >= 0) "+" else ""
                        Text(
                            "$sign€${"%.0f".format(state.monthlyDelta / 100.0)} this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = deltaColor
                        )
                    }
                }
            }
        }

        // Vico line chart
        if (state.snapshots.size >= 2) {
            item {
                NetWorthChart(state.snapshots)
            }
        }

        // Assets section
        if (state.assetAccounts.isNotEmpty()) {
            item {
                Text("Assets", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            }
            state.assetAccounts.entries.forEach { (type, accounts) ->
                item {
                    AccountTypeSection(
                        typeName  = type.displayName(),
                        accounts  = accounts,
                        expanded  = type in state.expandedTypes,
                        onToggle  = { viewModel.toggleType(type) }
                    )
                }
            }
        }

        // Liabilities section
        if (state.liabilityAccounts.isNotEmpty()) {
            item {
                Text("Liabilities", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                AccountTypeSection(
                    typeName = "Liabilities",
                    accounts = state.liabilityAccounts,
                    expanded = AccountType.LIABILITY in state.expandedTypes,
                    onToggle = { viewModel.toggleType(AccountType.LIABILITY) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun NetWorthChart(snapshots: List<NetWorthSnapshot>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(snapshots) {
        modelProducer.runTransaction {
            lineSeries { series(snapshots.reversed().map { it.netWorth.toFloat() / 100f }) }
        }
    }

    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Monthly trajectory", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            CartesianChartHost(
                chart    = rememberCartesianChart(rememberLineCartesianLayer()),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
        }
    }
}

@Composable
private fun AccountTypeSection(
    typeName: String,
    accounts: List<Account>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val total = accounts.sumOf { it.currentBalance }
    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(typeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "€${"%.0f".format(total / 100.0)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = SurfaceColor)
                accounts.forEach { account ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(account.name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            "€${"%.0f".format(account.currentBalance / 100.0)}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize   = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun AccountType.displayName(): String = when (this) {
    AccountType.CHECKING   -> "Checking"
    AccountType.SAVINGS    -> "Savings"
    AccountType.INVESTMENT -> "Investments"
    AccountType.PROPERTY   -> "Property"
    AccountType.LIABILITY  -> "Liabilities"
}

// ─── Cash Flow Forecast Tab ───────────────────────────────────────────────────

@Composable
private fun CashFlowForecastTab(viewModel: CashFlowForecastViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("30-day cash flow projection", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    if (state.points.isNotEmpty()) {
                        CashFlowChart(state.points, Modifier.fillMaxWidth().height(160.dp))
                    } else {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("Computing…", color = TextSecondary)
                        }
                    }
                }
            }
        }

        if (state.upcomingBills.isNotEmpty()) {
            item {
                Text("Upcoming bills", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            }
            items(state.upcomingBills) { bill ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(bill.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (bill.daysUntil == 0) "Today" else "In ${bill.daysUntil} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (bill.daysUntil <= 3) AmberWarn else TextSecondary
                        )
                    }
                    Text(
                        "−€${"%.2f".format(bill.amountCents / 100.0)}",
                        fontFamily = JetBrainsMonoFamily,
                        color      = RedOver,
                        fontWeight = FontWeight.Medium
                    )
                }
                HorizontalDivider(color = SurfaceVar, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun CashFlowChart(points: List<CashFlowPoint>, modifier: Modifier) {
    val accentColor = Accent
    val bandColor   = Accent.copy(alpha = 0.12f)
    val lineColor   = RedOver

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height

        val allVals = points.flatMap { listOf(it.lower, it.upper) }
        val minV = allVals.minOrNull()!!.toFloat()
        val maxV = allVals.maxOrNull()!!.toFloat()
        val range = (maxV - minV).coerceAtLeast(1f)

        fun xOf(i: Int) = i.toFloat() / (points.size - 1).coerceAtLeast(1) * w
        fun yOf(v: Long) = h - (v.toFloat() - minV) / range * h

        // Confidence band fill
        val upperPath = Path()
        val lowerPath = Path()
        points.forEachIndexed { i, p ->
            val x = xOf(i)
            if (i == 0) { upperPath.moveTo(x, yOf(p.upper)); lowerPath.moveTo(x, yOf(p.lower)) }
            else        { upperPath.lineTo(x, yOf(p.upper)); lowerPath.lineTo(x, yOf(p.lower)) }
        }
        // Close band
        val bandPath = Path().apply {
            addPath(upperPath)
            points.indices.reversed().forEach { i ->
                lineTo(xOf(i), yOf(points[i].lower))
            }
            close()
        }
        drawPath(bandPath, brush = Brush.verticalGradient(listOf(bandColor, bandColor)))

        // Main balance line
        val mainPath = Path()
        points.forEachIndexed { i, p ->
            val x = xOf(i); val y = yOf(p.balance)
            if (i == 0) mainPath.moveTo(x, y) else mainPath.lineTo(x, y)
        }
        drawPath(mainPath, color = accentColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Zero line
        if (minV < 0 && maxV > 0) {
            val zeroY = yOf(0L)
            drawLine(lineColor.copy(alpha = 0.4f), Offset(0f, zeroY), Offset(w, zeroY),
                strokeWidth = 1.dp.toPx())
        }
    }
}

// ─── FIRE Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun FireTab(viewModel: FireViewModel) {
    val state by viewModel.uiState.collectAsState()

    var annualExpensesText by remember(state.annualExpenses) {
        mutableStateOf("%.2f".format(state.annualExpenses / 100.0))
    }
    var swrText     by remember(state.swrPercent)  { mutableStateOf(state.swrPercent.toString()) }
    var returnText  by remember(state.realReturn)  { mutableStateOf(state.realReturn.toString()) }
    var birthText   by remember(state.birthYear)   { mutableStateOf(state.birthYear.toString()) }
    var aowText     by remember(state.aowMonthly)  { mutableStateOf("%.2f".format(state.aowMonthly / 100.0)) }
    var pensionText by remember(state.pensionMonthly) { mutableStateOf("%.2f".format(state.pensionMonthly / 100.0)) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FI Progress card
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("FI Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Current portfolio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("€${"%.0f".format(state.currentPortfolio / 100.0)}",
                            fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("FI number", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("€${"%.0f".format(state.fiNumber / 100.0)}",
                            fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color    = Accent,
                    trackColor = SurfaceVar
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${"%.1f".format(state.progress * 100)}% of FI",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent
                )
            }
        }

        // 2×2 metric grid
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Savings Rate", "${"%.1f".format(state.savingsRate * 100)}%", Modifier.weight(1f))
            val ytfi = state.yearsToFi
            MetricCard(
                "Years to FI",
                if (ytfi.isFinite() && ytfi < 99) "${"%.1f".format(ytfi)} yr" else "∞",
                Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Coast FI", "€${"%.0f".format(state.coastFi / 100.0)}", Modifier.weight(1f))
            val runway = state.ffRunway
            MetricCard(
                "FF Runway",
                if (runway.isFinite()) "${"%.0f".format(runway)} mo" else "∞",
                Modifier.weight(1f)
            )
        }

        // Scenario planner
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceVar), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Scenario: +€200/month savings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Current", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        val y = state.yearsToFi
                        Text(if (y.isFinite() && y < 99) "${"%.1f".format(y)} yr" else "∞",
                            fontFamily = JetBrainsMonoFamily, fontSize = 18.sp)
                    }
                    Text("→", fontSize = 20.sp, color = TextSecondary, modifier = Modifier.align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("With +€200/mo", style = MaterialTheme.typography.labelSmall, color = Accent)
                        val sy = state.scenarioYearsToFi
                        Text(if (sy.isFinite() && sy < 99) "${"%.1f".format(sy)} yr" else "∞",
                            fontFamily = JetBrainsMonoFamily, fontSize = 18.sp, color = Accent)
                    }
                }
            }
        }

        // Settings inputs
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceVar), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FIRE Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                FireInputRow("Annual expenses (€)", annualExpensesText) { annualExpensesText = it }
                FireInputRow("Safe withdrawal rate (%)", swrText) { swrText = it }
                FireInputRow("Assumed real return (%)", returnText) { returnText = it }
                FireInputRow("Birth year", birthText, KeyboardType.Number) { birthText = it }

                Button(
                    onClick = {
                        viewModel.saveSettings(annualExpensesText, swrText, returnText, birthText, aowText, pensionText)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save") }
            }
        }

        // AOW / Pension section
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceVar), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AOW & Pension", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "From mijnpensioenoverzicht.nl",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                FireInputRow("AOW (€/month)", aowText) { aowText = it }
                FireInputRow("Occupational pension (€/month)", pensionText) { pensionText = it }

                HorizontalDivider(color = SurfaceColor, modifier = Modifier.padding(vertical = 4.dp))

                val needed = state.annualExpenses / 12
                val covered = state.aowMonthly + state.pensionMonthly
                val gap     = state.gapMonthly
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Monthly need", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("€${"%.0f".format(needed / 100.0)}", fontFamily = JetBrainsMonoFamily, fontSize = 13.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("AOW + pension", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("€${"%.0f".format(covered / 100.0)}", fontFamily = JetBrainsMonoFamily, fontSize = 13.sp, color = Accent)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Portfolio must cover", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text("€${"%.0f".format(gap / 100.0)}/mo",
                        fontFamily = JetBrainsMonoFamily, fontSize = 13.sp,
                        color = if (gap > 0) AmberWarn else Accent)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceVar), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun FireInputRow(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    onValueChange: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            singleLine      = true,
            modifier        = Modifier.width(120.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle       = LocalTextStyle.current.copy(fontFamily = JetBrainsMonoFamily),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Background,
                unfocusedContainerColor = Background
            )
        )
    }
}
