package com.vrijgeld.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.vrijgeld.ui.navigation.Screen
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context     = LocalContext.current
    val accounts    by viewModel.accounts.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val notifPrefs  by viewModel.notifPrefs.collectAsState()
    val categories  by viewModel.categories.collectAsState()

    var selectedAccIdx by remember { mutableIntStateOf(0) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val accId = accounts.getOrNull(selectedAccIdx)?.id ?: return@rememberLauncherForActivityResult
        viewModel.importCamt053(uri, context, accId)
    }

    if (importState is ImportState.Success || importState is ImportState.Error) {
        LaunchedEffect(importState) {
            kotlinx.coroutines.delay(3_000)
            viewModel.resetImportState()
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

            Button(
                onClick  = { filePicker.launch(arrayOf("text/xml", "application/xml", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = importState !is ImportState.Loading && accounts.isNotEmpty()
            ) {
                if (importState is ImportState.Loading)
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else
                    Text("Import CAMT.053 file")
            }

            when (val s = importState) {
                is ImportState.Success ->
                    Text("✓ Imported ${s.count} new transactions",
                        color = MaterialTheme.colorScheme.primary)
                is ImportState.Error ->
                    Text("✗ ${s.message}", color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            HorizontalDivider()

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
        }
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
            value          = text,
            onValueChange  = { text = it },
            prefix         = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine     = true,
            modifier       = Modifier.width(110.dp),
            colors         = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = com.vrijgeld.ui.theme.Background,
                unfocusedContainerColor = com.vrijgeld.ui.theme.Background
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
