package com.vrijgeld.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vrijgeld.ui.add.AddTransactionScreen
import com.vrijgeld.ui.budget.BudgetScreen
import com.vrijgeld.ui.home.HomeScreen
import com.vrijgeld.ui.settings.SettingsScreen
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Surface

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Add      : Screen("add")
    object Budget   : Screen("budget")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = com.vrijgeld.ui.theme.Background,
        bottomBar      = { AppBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route)     { HomeScreen() }
            composable(Screen.Add.route)      { AddTransactionScreen(navController) }
            composable(Screen.Budget.route)   { BudgetScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current   = backStack?.destination?.route

    NavigationBar(containerColor = Surface) {
        NavigationBarItem(
            selected = current == Screen.Settings.route,
            onClick  = { navController.navigateTo(Screen.Settings.route) },
            icon     = { Icon(Icons.Filled.Settings, "Settings") },
            label    = { Text("Settings") },
            colors   = navItemColors()
        )
        // Centre: elevated Add button
        NavigationBarItem(
            selected = current == Screen.Add.route,
            onClick  = { navController.navigateTo(Screen.Add.route) },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Accent,
                    tonalElevation = 6.dp
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        tint               = Color.Black,
                        modifier           = Modifier.padding(12.dp)
                    )
                }
            },
            label  = { Text("Add") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = current == Screen.Budget.route,
            onClick  = { navController.navigateTo(Screen.Budget.route) },
            icon     = { Icon(Icons.Filled.PieChart, "Budget") },
            label    = { Text("Budget") },
            colors   = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = Accent,
    selectedTextColor   = Accent,
    indicatorColor      = Accent.copy(alpha = 0.15f),
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

private fun NavController.navigateTo(route: String) = navigate(route) {
    popUpTo(Screen.Home.route) { saveState = true }
    launchSingleTop = true
    restoreState    = true
}
