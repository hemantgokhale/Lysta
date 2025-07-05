package dev.hgokhale.lists.app

import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import dev.hgokhale.lists.home.HomeScreen
import dev.hgokhale.lists.lyst.LystScreen

sealed class NavigationDestination(val route: String) {
    data object Home : NavigationDestination("home")
    data object Lyst : NavigationDestination("list/{listId}") {
        fun routeFor(listId: String) = "list/$listId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    navigationViewModel: NavigationViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Home.route,
        modifier = Modifier.imePadding()
    ) {
        composable(NavigationDestination.Home.route) { HomeScreen(navigationViewModel) }
        composable(NavigationDestination.Lyst.route) { backStackEntry ->
            backStackEntry.arguments?.read { getStringOrNull("listId") }?.let {
                LystScreen(listId = it, navigationViewModel = navigationViewModel)
            }
        }
    }
}
