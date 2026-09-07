package com.somna.sleeptracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.somna.sleeptracker.ui.dashboard.DashboardScreen
import com.somna.sleeptracker.ui.dashboard.DashboardViewModel

object SomnaDestinations {
    const val DASHBOARD = "dashboard"
}

@Composable
fun SomnaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = SomnaDestinations.DASHBOARD,
        modifier = modifier
    ) {
        composable(SomnaDestinations.DASHBOARD) {
            val state by dashboardViewModel.state.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                onIntent = dashboardViewModel::handleIntent
            )
        }
    }
}
