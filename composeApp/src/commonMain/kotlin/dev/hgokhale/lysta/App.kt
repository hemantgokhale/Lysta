package dev.hgokhale.lysta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: LystViewModel = viewModel { LystViewModel() }) {
    MaterialTheme {
        // We add this colum to center the app content on a large screen e.g. desktop or web
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Scaffold(
                modifier = Modifier.widthIn(max = 450.dp),
                topBar = { TopAppBar(title = { Text("Lists") }) },
                floatingActionButton = { DraggableFAB { println("FAB Clicked") } },
            ) { paddingValues ->
                Home(
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel
                )
            }
        }
    }
}