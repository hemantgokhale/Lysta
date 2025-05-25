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
import dev.hgokhale.lysta.lyst.LystScreen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

sealed class NavigationDestination(val route: String) {
    data object Home : NavigationDestination("home")
    data object Lyst : NavigationDestination("list/{listId}") {
        fun routeFor(listId: String) = "list/$listId"
    }
}

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

sealed interface NavigationEvent {
    data class Navigate(val route: String) : NavigationEvent
    data object NavigateBack : NavigationEvent
}

object NavigationEventBus {
    private val _events = Channel<NavigationEvent>(capacity = Channel.CONFLATED)
    val events: ReceiveChannel<NavigationEvent> = _events

    suspend fun send(event: NavigationEvent) {
        _events.send(event)
    }
}