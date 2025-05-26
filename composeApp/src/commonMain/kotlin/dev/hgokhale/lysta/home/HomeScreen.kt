package dev.hgokhale.lysta.home

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hgokhale.lysta.app.ScaffoldViewModel
import dev.hgokhale.lysta.getPlatform
import dev.hgokhale.lysta.utils.Highlightable
import dev.hgokhale.lysta.utils.LoadingIndicator
import dev.hgokhale.lysta.utils.ScrollToNewItemEffect
import dev.hgokhale.lysta.utils.SwipeToDeleteItem
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreen(
    scaffoldViewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel { HomeViewModel() }
) {
    val loaded by homeViewModel.loaded.collectAsStateWithLifecycle()
    if (loaded) {
        ConfigureScaffold(scaffoldViewModel = scaffoldViewModel, homeViewModel = homeViewModel)
        Home(homeViewModel = homeViewModel, modifier = modifier)
    } else {
        LoadingIndicator(modifier = modifier)
    }
}

@Composable
private fun ConfigureScaffold(scaffoldViewModel: ScaffoldViewModel, homeViewModel: HomeViewModel) {
    LaunchedEffect(Unit) {
        scaffoldViewModel.topBarTitle.value = "My lists"
        scaffoldViewModel.onTitleChange.value = null
        scaffoldViewModel.showBackButton.value = false
        scaffoldViewModel.fabAction.value = { homeViewModel.createList() }
        scaffoldViewModel.topBarActions.value = listOf()
    }
}

@Composable
private fun Home(homeViewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val lists by homeViewModel.lists.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        homeViewModel.moveList(from.index, to.index)
    }

    ScrollToNewItemEffect(homeViewModel.newItem, lazyListState)

    LazyColumn(modifier = modifier.fillMaxSize().focusProperties { canFocus = false }, state = lazyListState) {
        items(items = lists, key = { item -> item.id }) { item ->
            ReorderableItem(state = reorderableLazyListState, key = item.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                val reorderableCollectionItemScope = this
                Surface(shadowElevation = elevation) {
                    val onDelete = { homeViewModel.deleteList(item.id) }
                    SwipeToDeleteItem(onDelete = onDelete) {
                        Highlightable(item) { modifier ->
                            HomeItem(homeViewModel, item, onDelete, reorderableCollectionItemScope, modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeItem(
    homeViewModel: HomeViewModel,
    item: HomeViewModel.UIItem,
    onDelete: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    modifier: Modifier = Modifier,
) {
    val isMobile = remember { getPlatform().isMobile }
    var isHovered by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable { homeViewModel.onListClicked(item.id) }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                            else -> Unit
                        }
                    }
                }
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.name, modifier = Modifier.weight(1f))
        if (!isMobile && isHovered) {
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
