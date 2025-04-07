package dev.hgokhale.lysta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: LystViewModel, navController: NavController) {
    LaunchedEffect(Unit) { viewModel.goHome() }
    val uiState by viewModel.uiState.collectAsState()

    (uiState as? LystViewModel.UIState.Home)
        ?.let { Home(modifier = modifier, viewModel = viewModel, navController = navController) }
        ?: LoadingIndicator(modifier = modifier)
}

@Composable
private fun Home(modifier: Modifier = Modifier, viewModel: LystViewModel, navController: NavController) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        items(
            items = viewModel.lists,
            key = { item -> item.id }
        ) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name.value,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate(Screen.Lyst.routeFor(item.id)) }
                )
                IconButton(onClick = { viewModel.deleteList(item.id) }) {
                    Icon(painter = rememberVectorPainter(image = Icons.Default.Delete), contentDescription = "Delete", tint = Color.Black)
                }
            }
        }
    }
}