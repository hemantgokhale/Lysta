package dev.hgokhale.lysta

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow

/**
 * A composable function that observes a flow of new item indices and scrolls a `LazyListState` to display the newly added item
 * if it is not already visible in the current viewport.
 *
 * @param newItemFlow A `SharedFlow` that emits the index of newly added items.
 * @param lazyListState The `LazyListState` associated with the lazy list to be scrolled.
 */
@Composable
fun ScrollToNewItemEffect(newItemFlow: SharedFlow<Int>, lazyListState: LazyListState) {
    LaunchedEffect(Unit) {
        newItemFlow.collect { index ->
            val visibleRange = lazyListState.layoutInfo.visibleItemsInfo.map { it.index }
            // We need special handling for index 0.
            // When you delete and restore the very first item in the list, the visibleRange includes 0.
            // It appears the visibleRange is computed using the pre-restored state of the list.
            if (index == 0 || index !in visibleRange) {
                lazyListState.scrollToItem(index)
            }
        }
    }
}

