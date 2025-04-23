package dev.hgokhale.lysta

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveList(from.index, to.index)
    }

    LazyColumn(modifier = modifier.fillMaxSize(), state = lazyListState) {
        items(items = lists, key = { item -> item.id }) { item ->
            ReorderableItem(state = reorderableLazyListState, key = item.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                Surface(shadowElevation = elevation) {
                    DismissibleItem(viewModel, item, this)
                }
            }
        }
    }
}

@Composable
private fun DismissibleItem(viewModel: LystViewModel, item: Lyst, scope: ReorderableCollectionItemScope) {
    val currentItem by rememberUpdatedState(item)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                viewModel.deleteList(currentItem.id)
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Row(
                modifier = Modifier.fillMaxSize().background(color),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.padding(16.dp), tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            modifier = Modifier.clickable { viewModel.onListClicked(currentItem.id) }.padding(horizontal = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currentItem.name.value, modifier = Modifier.weight(1f))
            IconButton(onClick = { }, modifier = with(scope) { Modifier.draggableHandle() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_drag_handle),
                    contentDescription = "Move item",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}