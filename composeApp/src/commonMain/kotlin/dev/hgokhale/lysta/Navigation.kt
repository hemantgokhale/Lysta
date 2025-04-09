package dev.hgokhale.lysta

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LystViewModel.UIEvent.Navigate -> navController.navigate(event.route)
                LystViewModel.UIEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) { HomeScreen(viewModel = viewModel) }
        composable(Screen.Lyst.route) { backStackEntry ->
            backStackEntry.arguments?.getString("listId")?.let {
                LystScreen(listId = it, viewModel = viewModel)
            }
        }
    }
}
