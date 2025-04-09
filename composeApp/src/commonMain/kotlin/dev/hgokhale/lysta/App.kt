package dev.hgokhale.lysta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

@Composable
fun App(viewModel: LystViewModel = viewModel { LystViewModel() }) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LystaTheme {
        Column( // We add this colum to center the app content on a large screen e.g. desktop or web
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Scaffold(
                modifier = Modifier.widthIn(max = 450.dp),
                topBar = { TopBar(viewModel = viewModel) },
                floatingActionButton = { if (uiState.showFAB) DraggableFAB { viewModel.onFabClicked() } },
            ) { paddingValues ->
                NavGraph(paddingValues, navController, viewModel)
            }
        }
    }
}
