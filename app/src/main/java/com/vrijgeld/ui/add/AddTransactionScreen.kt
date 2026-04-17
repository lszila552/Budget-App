package com.vrijgeld.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface
import com.vrijgeld.ui.theme.SurfaceVar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val accounts   by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var amountText   by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var selectedAccIdx  by remember { mutableIntStateOf(0) }
    var selectedCatIdx  by remember { mutableIntStateOf(0) }
    var isExpense    by remember { mutableStateOf(true) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expense / Income toggle
            Row {
                FilterChip(selected = isExpense,  onClick = { isExpense = true  }, label = { Text("Expense") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = !isExpense, onClick = { isExpense = false }, label = { Text("Income") })
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceVar, unfocusedContainerColor = SurfaceVar)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceVar, unfocusedContainerColor = SurfaceVar)
            )

            if (accounts.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = accounts.getOrNull(selectedAccIdx)?.name ?: "Select account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceVar, unfocusedContainerColor = SurfaceVar)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceVar)) {
                        accounts.forEachIndexed { i, acc ->
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = { selectedAccIdx = i; expanded = false })
                        }
                    }
                }
            }

            if (categories.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = categories.getOrNull(selectedCatIdx)?.let { "${it.icon} ${it.name}" } ?: "Uncategorized",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceVar, unfocusedContainerColor = SurfaceVar)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceVar)) {
                        categories.forEachIndexed { i, cat ->
                            DropdownMenuItem(text = { Text("${cat.icon} ${cat.name}") },
                                onClick = { selectedCatIdx = i; expanded = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val cents = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        .let { if (isExpense) -it else it }
                    val accId = accounts.getOrNull(selectedAccIdx)?.id ?: return@Button
                    val catId = categories.getOrNull(selectedCatIdx)?.id
                    viewModel.save(accId, catId, cents, description, System.currentTimeMillis()) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = amountText.isNotBlank() && accounts.isNotEmpty()
            ) { Text("Save") }
        }
    }
}
