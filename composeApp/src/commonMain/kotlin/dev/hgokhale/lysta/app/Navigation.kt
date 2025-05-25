package dev.hgokhale.lysta.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import dev.hgokhale.lysta.home.HomeScreen
import dev.hgokhale.lysta.home.LystaViewModel
import dev.hgokhale.lysta.home.NavigationDestination
import dev.hgokhale.lysta.lyst.LystScreen

@Composable
fun NavGraph(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: LystaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Home.route,
        modifier = Modifier.padding(paddingValues).imePadding()
    ) {
        composable(NavigationDestination.Home.route) { HomeScreen(viewModel = viewModel) }
        composable(NavigationDestination.Lyst.route) { backStackEntry ->
            backStackEntry.arguments?.read { getStringOrNull("listId") }?.let {
                LystScreen(listId = it, viewModel = viewModel)
            }
        }
    }
}
