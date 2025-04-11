package dev.hgokhale.lysta

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: LystViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(NavigationDestination.Home.route) { HomeScreen(viewModel = viewModel) }
        composable(NavigationDestination.Lyst.route) { backStackEntry ->
            backStackEntry.arguments?.getString("listId")?.let {
                LystScreen(listId = it, viewModel = viewModel)
            }
        }
    }
}
