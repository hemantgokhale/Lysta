package dev.hgokhale.lysta

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
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
    val isMobile = remember { getPlatform().isMobile }
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveList(from.index, to.index)
    }

    LazyColumn(modifier = modifier.fillMaxSize(), state = lazyListState) {
        items(items = lists, key = { item -> item.id }) { item ->
            ReorderableItem(state = reorderableLazyListState, key = item.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                val reorderableCollectionItemScope = this
                Surface(shadowElevation = elevation) {
                    val onDelete = { viewModel.deleteList(item.id) }
                    SwipeToDeleteItem(onDelete = onDelete) {
                        Row(
                            modifier = Modifier.background(MaterialTheme.colorScheme.background).clickable { viewModel.onListClicked(item.id) }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.name.value, modifier = Modifier.weight(1f))
                            if (!isMobile) {
                                IconButton(onClick = onDelete) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete item",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                    )
                                }
                            }
                            IconButton(onClick = { }, modifier = with(reorderableCollectionItemScope) { Modifier.draggableHandle() }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_drag_handle),
                                    contentDescription = "Move item",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
