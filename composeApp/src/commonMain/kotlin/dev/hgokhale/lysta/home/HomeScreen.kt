package dev.hgokhale.lysta.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hgokhale.lysta.app.NavigationViewModel
import dev.hgokhale.lysta.getPlatform
import dev.hgokhale.lysta.utils.ConfigureSnackbar
import dev.hgokhale.lysta.utils.DraggableFAB
import dev.hgokhale.lysta.utils.DraggableHandle
import dev.hgokhale.lysta.utils.Highlightable
import dev.hgokhale.lysta.utils.LoadingIndicator
import dev.hgokhale.lysta.utils.LystaSnackbar
import dev.hgokhale.lysta.utils.ScrollToNewItemEffect
import dev.hgokhale.lysta.utils.SwipeToDeleteItem
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel { HomeViewModel(navigationViewModel) },
) {
    val loaded by homeViewModel.loaded.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        homeViewModel.refreshListNames()
    }

    ConfigureSnackbar(homeViewModel, snackbarHostState)

    if (loaded) {
        Scaffold(
            topBar = {
                val textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground)
                TopAppBar(title = { Text("My lists", style = textStyle) })
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, snackbar = { LystaSnackbar(it) }) },
            floatingActionButton = { DraggableFAB { homeViewModel.createList() } },
        ) { paddingValues ->
            Home(homeViewModel = homeViewModel, modifier = modifier.padding(paddingValues))
        }
    } else {
        LoadingIndicator(modifier = modifier)
    }
}

@Composable
private fun Home(homeViewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val lists by homeViewModel.lists.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                homeViewModel.createList()
            }
    ) {
        if (lists.isEmpty()) {
            EmptyHome()
        } else {
            Lists(homeViewModel = homeViewModel, modifier = modifier)
        }
    }
}

@Composable
private fun ColumnScope.EmptyHome(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.weight(1f))
    Text(
        text = "Welcome!\nAdd a list by tapping anywhere in the empty area or the '+' button below.",
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
    )
    Spacer(modifier = modifier.weight(2f))
}

@Composable
private fun Lists(homeViewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val lists by homeViewModel.lists.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        homeViewModel.moveList(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    ScrollToNewItemEffect(homeViewModel.newItem, lazyListState)
    LazyColumn(
        modifier = modifier.focusProperties { canFocus = false },
        state = lazyListState
    ) {
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
    val lists by homeViewModel.lists.collectAsStateWithLifecycle()
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
        DraggableHandle(
            reorderableCollectionItemScope = reorderableCollectionItemScope,
            show = lists.size > 1
        )

    }
}
