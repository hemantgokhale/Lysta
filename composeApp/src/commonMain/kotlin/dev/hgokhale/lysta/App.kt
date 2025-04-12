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

@Composable
fun App(viewModel: LystViewModel = viewModel { LystViewModel() }) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LystViewModel.UIEvent.Navigate -> navController.navigate(event.route)
                is LystViewModel.UIEvent.NavigateBack -> navController.popBackStack()
                is LystViewModel.UIEvent.Snackbar -> {
                    if (snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        ) == SnackbarResult.ActionPerformed
                    ) {
                        event.action?.let { it() }
                    }
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
