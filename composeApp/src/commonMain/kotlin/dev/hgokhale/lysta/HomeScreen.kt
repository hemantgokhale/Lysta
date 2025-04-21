package dev.hgokhale.lysta

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: LystViewModel) {
    LaunchedEffect(Unit) { viewModel.goHome() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    (uiState as? LystViewModel.UIState.Home)
        ?.let { Home(modifier = modifier, viewModel = viewModel) }
        ?: LoadingIndicator(modifier = modifier)
}

@Composable
private fun Home(modifier: Modifier = Modifier, viewModel: LystViewModel) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        items(
            items = lists,
            key = { item -> item.id }
        ) { item ->
            DismissibleItem(viewModel, item)
        }
    }
}

@Composable
private fun DismissibleItem(viewModel: LystViewModel, item: Lyst) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                viewModel.deleteList(item.id)
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by
            animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                    SwipeToDismissBoxValue.StartToEnd -> Color.Red
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Box(Modifier.fillMaxSize().background(color)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name.value,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.onListClicked(item.id) }
            )
        }
    }
}