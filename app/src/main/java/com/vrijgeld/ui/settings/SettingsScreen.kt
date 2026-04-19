package com.vrijgeld.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.data.repository.KEY_NOTIF_BILL_LOW_BALANCE
import com.vrijgeld.data.repository.KEY_NOTIF_SUBSCRIPTION_RENEWAL
import com.vrijgeld.data.repository.KEY_NOTIF_UNUSUAL_TX
import com.vrijgeld.data.repository.KEY_NOTIF_WEEKLY_PACE
import com.vrijgeld.data.model.AccountType
import com.vrijgeld.ui.navigation.Screen
import com.vrijgeld.ui.theme.ACCENT_NAMES
import com.vrijgeld.ui.theme.ACCENT_OPTIONS
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AppTheme
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context          = LocalContext.current
    val accounts         by viewModel.accounts.collectAsState()
    val importState      by viewModel.importState.collectAsState()
    val notifPrefs       by viewModel.notifPrefs.collectAsState()
    val categories       by viewModel.categories.collectAsState()
    val currentTheme     by viewModel.theme.collectAsState()
    val currentAccent    by viewModel.accentIndex.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val exportState      by viewModel.exportState.collectAsState()

    var selectedAccIdx by remember { mutableIntStateOf(0) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val accId = accounts.getOrNull(selectedAccIdx)?.id ?: return@rememberLauncherForActivityResult
        viewModel.importFile(uri, context, accId)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.exportJson(context, uri)
    }

    val importJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.importJson(context, uri)
    }

    if (importState is ImportState.Success || importState is ImportState.Error) {
        LaunchedEffect(importState) {
            kotlinx.coroutines.delay(3_000)
            viewModel.resetImportState()
        }
    }
    if (exportState is ImportState.Success || exportState is ImportState.Error) {
        LaunchedEffect(exportState) {
            kotlinx.coroutines.delay(3_000)
            viewModel.resetExportState()
        }
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Accent,
        checkedTrackColor = Accent.copy(alpha = 0.4f)
    )

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) { popUpTo(0) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Appearance ──────────────────────────────────────────
            Text("Appearance", style = MaterialTheme.typography.titleLarge)

            // Theme picker
            Text("Theme", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.values().forEach { theme ->
                    FilterChip(
                        selected = currentTheme == theme,
                        onClick  = { viewModel.setTheme(theme) },
                        label    = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Accent colour picker
            Text("Accent colour", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ACCENT_OPTIONS.forEachIndexed { i, color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { viewModel.setAccent(i) }
                            .then(
                                if (i == currentAccent)
                                    Modifier.border(2.dp, Color.White, CircleShape)
                                else Modifier
                            )
                    )
                }
            }

            HorizontalDivider()

            // ── Import ───────────────────────────────────────────────
            Text("Import", style = MaterialTheme.typography.titleLarge)

            if (accounts.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value          = accounts.getOrNull(selectedAccIdx)?.name ?: "",
                        onValueChange  = {},
                        readOnly       = true,
                        label          = { Text("Import into account") },
                        trailingIcon   = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier       = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        accounts.forEachIndexed { i, acc ->
                            DropdownMenuItem(text = { Text(acc.name) },
                                onClick = { selectedAccIdx = i; expanded = false })
                        }
                    }
                }
            }

            Text(
                "Export your transactions as CSV from your bank's website, or download a CAMT.053 XML file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick  = { filePicker.launch(arrayOf(
                    "text/csv", "text/comma-separated-values",
                    "text/xml", "application/xml", "*/*"
                )) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = importState !is ImportState.Loading && accounts.isNotEmpty()
            ) {
                if (importState is ImportState.Loading)
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else
                    Text("Import bank file (.csv or .xml)")
            }

            when (val s = importState) {
                is ImportState.Success -> {
                    val needsReview = s.count - s.categorized
                    Text(
                        "✓ Imported ${s.count} transactions " +
                        "(${s.categorized} auto-categorized, $needsReview need review)",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is ImportState.Error ->
                    Text("✗ ${s.message}", color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            HorizontalDivider()

            // ── Data export / import ─────────────────────────────────
            Text("Backup & Restore", style = MaterialTheme.typography.titleLarge)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = { exportLauncher.launch("vrijgeld_backup.json") },
                    modifier = Modifier.weight(1f),
                    enabled  = exportState !is ImportState.Loading
                ) { Text("Export JSON") }

                OutlinedButton(
                    onClick  = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f),
                    enabled  = exportState !is ImportState.Loading
                ) { Text("Import JSON") }
            }

            when (val s = exportState) {
                is ImportState.Success -> Text("✓ Done (${s.count} transactions)", color = MaterialTheme.colorScheme.primary)
                is ImportState.Error   -> Text("✗ ${s.message}", color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            HorizontalDivider()

            // ── Notifications ────────────────────────────────────────
            Text("Notifications", style = MaterialTheme.typography.titleLarge)

            NotifToggleRow(
                label    = "Weekly pace check",
                sublabel = "Sunday: most over-budget category",
                checked  = notifPrefs.weeklyPace,
                onToggle = { viewModel.setNotifPref(KEY_NOTIF_WEEKLY_PACE, it) },
                colors   = switchColors
            )
            NotifToggleRow(
                label    = "Bill due + low balance",
                sublabel = "2 days before charge, if balance is low",
                checked  = notifPrefs.billLowBalance,
                onToggle = { viewModel.setNotifPref(KEY_NOTIF_BILL_LOW_BALANCE, it) },
                colors   = switchColors
            )
            NotifToggleRow(
                label    = "Unusual transaction",
                sublabel = "Charge >2× normal for that category",
                checked  = notifPrefs.unusualTx,
                onToggle = { viewModel.setNotifPref(KEY_NOTIF_UNUSUAL_TX, it) },
                colors   = switchColors
            )
            NotifToggleRow(
                label    = "Subscription renewal",
                sublabel = "3 days before next expected charge",
                checked  = notifPrefs.subscriptionRenewal,
                onToggle = { viewModel.setNotifPref(KEY_NOTIF_SUBSCRIPTION_RENEWAL, it) },
                colors   = switchColors
            )

            HorizontalDivider()

            // ── Security ─────────────────────────────────────────────
            Text("Security", style = MaterialTheme.typography.titleLarge)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Biometric lock", style = MaterialTheme.typography.bodyMedium)
                    Text("Require fingerprint or PIN on open",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = biometricEnabled, onCheckedChange = viewModel::setBiometric, colors = switchColors)
            }

            // ── Manual account balances ──────────────────────────────
            val manualAccounts = accounts.filter {
                it.type == AccountType.INVESTMENT || it.type == AccountType.PROPERTY
            }
            if (manualAccounts.isNotEmpty()) {
                HorizontalDivider()
                Text("Manual Account Balances", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Update investment and property values (DEGIRO, IBKR, WOZ)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                manualAccounts.forEach { account ->
                    ManualBalanceRow(
                        name      = account.name,
                        balance   = account.currentBalance,
                        typeLabel = if (account.type == AccountType.INVESTMENT) "Investment" else "Property",
                        onSave    = { viewModel.updateAccountBalance(account.id, it) }
                    )
                }
            }

            // ── Budget defaults ──────────────────────────────────────
            if (categories.isNotEmpty()) {
                HorizontalDivider()

                Text("Budget Defaults", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Default monthly amounts used when allocating income",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                categories.filter { !it.isSinkingFund }.forEach { cat ->
                    BudgetDefaultRow(
                        icon    = cat.icon,
                        name    = cat.name,
                        amount  = cat.monthlyBudget,
                        label   = "Monthly budget",
                        onSave  = { viewModel.updateCategoryBudget(cat, it) }
                    )
                }

                val sinkingCats = categories.filter { it.isSinkingFund }
                if (sinkingCats.isNotEmpty()) {
                    Text(
                        "Sinking Fund Targets",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(top = 8.dp)
                    )
                    sinkingCats.forEach { cat ->
                        BudgetDefaultRow(
                            icon    = cat.icon,
                            name    = cat.name,
                            amount  = cat.sinkingFundTarget,
                            label   = "Target amount",
                            onSave  = { viewModel.updateSinkingFundTarget(cat, it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ManualBalanceRow(
    name: String,
    balance: Long,
    typeLabel: String,
    onSave: (String) -> Unit
) {
    var text by remember(balance) { mutableStateOf("%.2f".format(balance / 100.0)) }
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(typeLabel, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value           = text,
            onValueChange   = { text = it },
            prefix          = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine      = true,
            modifier        = Modifier.width(110.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Background,
                unfocusedContainerColor = Background
            )
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = { onSave(text) }) { Text("Save") }
    }
}

@Composable
private fun BudgetDefaultRow(
    icon: String,
    name: String,
    amount: Long?,
    label: String,
    onSave: (String) -> Unit
) {
    var text by remember(amount) {
        mutableStateOf(if (amount != null && amount > 0) "%.2f".format(amount / 100.0) else "")
    }
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value           = text,
            onValueChange   = { text = it },
            prefix          = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine      = true,
            modifier        = Modifier.width(110.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Background,
                unfocusedContainerColor = Background
            )
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = { onSave(text) }) { Text("Save") }
    }
}

@Composable
private fun NotifToggleRow(
    label: String,
    sublabel: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: SwitchColors
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(sublabel, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle, colors = colors)
    }
}
