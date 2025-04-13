package dev.hgokhale.lysta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: LystViewModel = viewModel { LystViewModel() }) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        for (event in viewModel.navigationEvents) {
            // If the user chooses to navigate away when a snackbar is showing, dismiss the snackbar.
            snackbarHostState.currentSnackbarData?.dismiss()
            when (event) {
                is LystViewModel.NavigationEvent.Navigate -> navController.navigate(event.route)
                is LystViewModel.NavigationEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        for (event in viewModel.snackbarEvents) {
            launch {
                // We show a snackbar to give the user an opportunity to undo an accidental delete. Given that,
                // we don't allow a queue of snackbars to build up. If the user proceeds to delete multiple items
                // one after another, they won't have to deal with a snackbar for every delete.
                // A side effect of this logic is that they will be able to undo only the last delete.
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    event.action?.let { it() }
                }
            }
        }
    }

    LystaTheme {
        Column( // We add this colum to center the app content on a large screen e.g. desktop or web
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Scaffold(
                modifier = Modifier.widthIn(max = 450.dp),
                topBar = { TopBar(viewModel = viewModel) },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = { if (uiState.showFAB) DraggableFAB { viewModel.onFabClicked() } },
            ) { paddingValues ->
                NavGraph(paddingValues, navController, viewModel)
            }
        }
    }
}
