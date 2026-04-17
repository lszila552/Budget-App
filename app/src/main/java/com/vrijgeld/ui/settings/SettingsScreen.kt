package com.vrijgeld.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.ui.navigation.Screen
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
                .background(Background),
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
                is ImportState.Error   ->
                    Text("✗ ${s.message}",
                        color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            HorizontalDivider()
            Text("No accounts yet — add one to start importing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
