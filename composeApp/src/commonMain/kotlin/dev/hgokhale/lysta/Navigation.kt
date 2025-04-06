package dev.hgokhale.lysta

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Lyst : Screen("list/{listId}") {
        fun routeFor(listId: String) = "list/$listId"
    }
}

@Composable
fun NavGraph(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: LystViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) { HomeScreen(viewModel = viewModel, navController = navController) }
        composable(Screen.Lyst.route) { backStackEntry ->
            backStackEntry.arguments?.getString("listId")?.let {
                LystScreen(listId = it, viewModel = viewModel)
            }
        }
    }
}
