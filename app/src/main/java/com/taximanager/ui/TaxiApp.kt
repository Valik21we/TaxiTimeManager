package com.taximanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taximanager.ui.screens.ShiftsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Обзор", Icons.Default.Home)
    object Shifts : Screen("shifts", "Смены", Icons.Default.Work)
    object Finance : Screen("finance", "Финансы", Icons.Default.AccountBalanceWallet)
    object Tasks : Screen("tasks", "Задачи", Icons.Default.CheckCircle)
    object Game : Screen("game", "Игра", Icons.Default.SportsEsports)
}

private val bottomScreens = listOf(
    Screen.Dashboard, Screen.Shifts, Screen.Finance, Screen.Tasks, Screen.Game
)

@Composable
fun TaxiApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val entry by navController.currentBackStackEntryAsState()
                val current = entry?.destination
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = current?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) { Placeholder("Обзор") }
            composable(Screen.Shifts.route) { ShiftsScreen() }
            composable(Screen.Finance.route) { Placeholder("Финансы") }
            composable(Screen.Tasks.route) { Placeholder("Задачи") }
            composable(Screen.Game.route) { Placeholder("Игра") }
        }
    }
}

@Composable
private fun Placeholder(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
