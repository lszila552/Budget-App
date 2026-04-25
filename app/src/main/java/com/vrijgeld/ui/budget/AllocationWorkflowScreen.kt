package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun AllocationWorkflowScreen(
    navController: NavController,
    viewModel: AllocationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showIncomeDialog by remember { mutableStateOf(false) }

    if (showIncomeDialog) {
        IncomeEditDialog(
            current   = state.totalIncome,
            onDismiss = { showIncomeDialog = false },
            onConfirm = { viewModel.overrideIncome(it); showIncomeDialog = false }
        )
    }

    LaunchedEffect(state.applied) {
        if (state.applied) navController.popBackStack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Allocate Income") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, null)
                    }
                },
                actions = {
                    IconButton(
                        onClick  = { viewModel.apply() },
                        enabled  = state.unallocated == 0L
                    ) {
                        Icon(Icons.Filled.Check, "Apply", tint = if (state.unallocated == 0L) Accent else TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Background)) {

            // Running unallocated banner
            val unallocColor = when {
                state.unallocated == 0L  -> Accent
                state.unallocated > 0L   -> AmberWarn
                else                     -> RedOver
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = SurfaceColor
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Total income", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick  = { showIncomeDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit income",
                                tint     = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        "€${"%.2f".format(state.totalIncome / 100.0)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Unallocated", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        "€${"%.2f".format(state.unallocated / 100.0)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = unallocColor
                    )
                    if (state.unallocated == 0L) {
                        Text("✓ Zero-based", style = MaterialTheme.typography.labelSmall, color = Accent)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expense categories
                if (state.expenseDrafts.isNotEmpty()) {
                    item {
                        Text(
                            "Expenses",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    itemsIndexed(state.expenseDrafts, key = { _, d -> d.categoryId }) { _, draft ->
                        AllocationRow(
                            draft    = draft,
                            onUpdate = { viewModel.updateDraft(draft.categoryId, it) }
                        )
                    }
                }

                // Savings categories
                if (state.savingsDrafts.isNotEmpty()) {
                    item {
                        Text(
                            "Savings",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                        )
                    }
                    itemsIndexed(state.savingsDrafts, key = { _, d -> d.categoryId }) { _, draft ->
                        AllocationRow(
                            draft    = draft,
                            onUpdate = { viewModel.updateDraft(draft.categoryId, it) }
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    Button(
                        onClick  = { viewModel.apply() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = state.unallocated == 0L
                    ) {
                        Text("Apply allocations")
                    }
                    if (state.unallocated != 0L) {
                        Text(
                            if (state.unallocated > 0) "€${"%.2f".format(state.unallocated / 100.0)} left to allocate"
                            else "Over-allocated by €${"%.2f".format(-state.unallocated / 100.0)}",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = if (state.unallocated > 0) AmberWarn else RedOver,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeEditDialog(
    current: Long,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(if (current > 0) "%.2f".format(current / 100.0) else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set income for this month") },
        text  = {
            OutlinedTextField(
                value           = text,
                onValueChange   = { text = it },
                prefix          = { Text("€") },
                label           = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AllocationRow(draft: AllocationDraft, onUpdate: (String) -> Unit) {
    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(draft.icon, fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(draft.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (draft.isSinkingFund) {
                    Text("sinking fund", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            OutlinedTextField(
                value          = draft.amountText,
                onValueChange  = onUpdate,
                prefix         = { Text("€") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine     = true,
                modifier       = Modifier.width(110.dp),
                textStyle      = LocalTextStyle.current.copy(fontFamily = JetBrainsMonoFamily),
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = Background,
                    unfocusedContainerColor = Background
                )
            )
        }
    }
}
