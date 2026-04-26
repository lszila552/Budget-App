package com.vrijgeld.ui.budget

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.RedOver
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.SurfaceVar
import com.vrijgeld.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    navController: NavController,
    viewModel: AddEditSubscriptionViewModel = hiltViewModel()
) {
    val state      by viewModel.state.collectAsState()
    val categories by viewModel.expenseCategories.collectAsState()
    val accounts   by viewModel.accounts.collectAsState()

    var showDatePicker   by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) navController.popBackStack()
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.nextDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setNextDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete subscription?") },
            text             = { Text("This will remove \"${state.name}\" from your subscriptions list.") },
            confirmButton    = {
                TextButton(onClick = { viewModel.delete(); showDeleteDialog = false }) {
                    Text("Delete", color = RedOver)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    val dateFmt = SimpleDateFormat("d MMMM yyyy", Locale("nl"))

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Subscription" else "Add Subscription") },
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
            modifier            = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name
            OutlinedTextField(
                value           = state.name,
                onValueChange   = { viewModel.setName(it) },
                label           = { Text("Service / merchant name") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier        = Modifier.fillMaxWidth(),
                colors          = outlinedColors()
            )

            // Amount
            OutlinedTextField(
                value           = state.amountText,
                onValueChange   = { viewModel.setAmount(it) },
                label           = { Text("Amount") },
                prefix          = { Text("€") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier        = Modifier.fillMaxWidth(),
                colors          = outlinedColors()
            )

            // Frequency
            Text("Frequency", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceFrequency.values().forEach { freq ->
                    FilterChip(
                        selected = state.frequency == freq,
                        onClick  = { viewModel.setFrequency(freq) },
                        label    = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Next payment date
            Text("Next payment date", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            OutlinedButton(
                onClick  = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dateFmt.format(Date(state.nextDateMillis)))
            }

            // Category (optional)
            if (categories.isNotEmpty()) {
                var catExpanded by remember { mutableStateOf(false) }
                val selectedCat = categories.find { it.id == state.categoryId }
                ExposedDropdownMenuBox(
                    expanded         = catExpanded,
                    onExpandedChange = { catExpanded = it },
                    modifier         = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = selectedCat?.let { "${it.icon} ${it.name}" } ?: "None",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Category (optional)") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier      = Modifier.menuAnchor().fillMaxWidth(),
                        colors        = outlinedColors()
                    )
                    ExposedDropdownMenu(
                        expanded         = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier         = Modifier.background(SurfaceVar)
                    ) {
                        DropdownMenuItem(
                            text    = { Text("None") },
                            onClick = { viewModel.setCategory(null); catExpanded = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text("${cat.icon} ${cat.name}") },
                                onClick = { viewModel.setCategory(cat.id); catExpanded = false }
                            )
                        }
                    }
                }
            }

            // Account (optional)
            if (accounts.isNotEmpty()) {
                var accExpanded by remember { mutableStateOf(false) }
                val selectedAcc = accounts.find { it.id == state.accountId } ?: accounts.first()
                ExposedDropdownMenuBox(
                    expanded         = accExpanded,
                    onExpandedChange = { accExpanded = it },
                    modifier         = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = if (state.accountId != null) selectedAcc.name else accounts.first().name,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Account (optional)") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(accExpanded) },
                        modifier      = Modifier.menuAnchor().fillMaxWidth(),
                        colors        = outlinedColors()
                    )
                    ExposedDropdownMenu(
                        expanded         = accExpanded,
                        onDismissRequest = { accExpanded = false },
                        modifier         = Modifier.background(SurfaceVar)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text    = { Text(acc.name) },
                                onClick = { viewModel.setAccount(acc.id); accExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick  = { viewModel.save() },
                enabled  = state.name.isNotBlank() && state.amountText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.isEditing) "Save changes" else "Add subscription")
            }

            if (viewModel.isEditing) {
                OutlinedButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = RedOver)
                ) {
                    Text("Delete subscription")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = SurfaceVar,
    unfocusedContainerColor = SurfaceVar
)
