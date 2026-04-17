package com.vrijgeld.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vrijgeld.ui.components.CategoryGrid
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface
import com.vrijgeld.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewQueueScreen(
    navController: NavController,
    viewModel: ReviewQueueViewModel = hiltViewModel()
) {
    val state      by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val fmt        = NumberFormat.getCurrencyInstance(Locale("nl", "NL"))

    LaunchedEffect(state.done) {
        if (state.done) navController.popBackStack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Review Imports (${state.remaining})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        val tx = state.current
        if (tx == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            return@Scaffold
        }

        Column(
            modifier            = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Transaction card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors   = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(tx.description, style = MaterialTheme.typography.titleMedium)
                    Text(
                        fmt.format(tx.amount / 100.0),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (tx.amount < 0) MaterialTheme.colorScheme.error else Accent
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Pick a category",
                style    = MaterialTheme.typography.labelLarge,
                color    = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.Start)
            )

            CategoryGrid(
                categories = categories,
                selectedId = null,
                onSelect   = { viewModel.assign(it) },
                modifier   = Modifier.fillMaxWidth().weight(1f)
            )

            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { viewModel.skip() },
                    modifier = Modifier.weight(1f)
                ) { Text("Skip") }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
