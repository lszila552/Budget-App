package com.vrijgeld.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.ui.components.CategoryGrid
import com.vrijgeld.ui.components.NumericKeypad
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.SurfaceVar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsState()
    val accounts    by viewModel.accounts.collectAsState()
    val categories  by viewModel.orderedCategories.collectAsState()

    var showNote by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Type pill toggles
            Row(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillToggle(
                    label    = "Expense",
                    selected = state.isExpense,
                    onClick  = { viewModel.toggleExpense(true) },
                    modifier = Modifier.weight(1f)
                )
                PillToggle(
                    label    = "Income",
                    selected = !state.isExpense,
                    onClick  = { viewModel.toggleExpense(false) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Amount display
            Box(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text       = "€ ${state.amountDisplay}",
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (state.isExpense) MaterialTheme.colorScheme.error else Accent
                )
            }

            // Custom keypad
            NumericKeypad(
                onKeyPress = { viewModel.onKey(it) },
                modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Recurring toggle
            Row(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recurring", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked         = state.isRecurring,
                    onCheckedChange = { viewModel.toggleRecurring(it) },
                    colors          = SwitchDefaults.colors(checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.4f))
                )
            }

            AnimatedVisibility(visible = state.isRecurring) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecurrenceFrequency.values().forEach { freq ->
                        FilterChip(
                            selected = state.frequency == freq,
                            onClick  = { viewModel.setFrequency(freq) },
                            label    = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Account picker
            if (accounts.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selectedAcc = accounts.find { it.id == state.selectedAccountId } ?: accounts.first()
                ExposedDropdownMenuBox(
                    expanded         = expanded,
                    onExpandedChange = { expanded = it },
                    modifier         = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value           = selectedAcc.name,
                        onValueChange   = {},
                        readOnly        = true,
                        label           = { Text("Account") },
                        trailingIcon    = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier        = Modifier.menuAnchor().fillMaxWidth(),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = SurfaceVar,
                            unfocusedContainerColor = SurfaceVar
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = expanded,
                        onDismissRequest = { expanded = false },
                        modifier         = Modifier.background(SurfaceVar)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text    = { Text(acc.name) },
                                onClick = { viewModel.selectAccount(acc.id); expanded = false }
                            )
                        }
                    }
                }
            }

            // Note field (collapsible)
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Note", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showNote = !showNote }) {
                    Icon(if (showNote) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                }
            }
            AnimatedVisibility(visible = showNote) {
                OutlinedTextField(
                    value           = state.note,
                    onValueChange   = { viewModel.setNote(it) },
                    placeholder     = { Text("Optional note") },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = SurfaceVar,
                        unfocusedContainerColor = SurfaceVar
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Category grid
            Text(
                "Category",
                style    = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            CategoryGrid(
                categories = categories,
                selectedId = state.selectedCategoryId,
                onSelect   = { viewModel.selectCategory(it.id) },
                modifier   = Modifier.fillMaxWidth().height(260.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick  = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                enabled  = state.amountDisplay != "0" && state.amountDisplay.isNotEmpty()
            ) {
                Text("Save")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PillToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick      = onClick,
        modifier     = modifier,
        shape        = RoundedCornerShape(50),
        color        = if (selected) Accent else SurfaceVar,
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text       = label,
                fontWeight = FontWeight.SemiBold,
                color      = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
