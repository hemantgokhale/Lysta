package dev.hgokhale.lysta.utils

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope

/**
 * A reusable composable that provides a draggable handle with haptic feedback.
 *
 * @param reorderableCollectionItemScope The scope for the reorderable collection item
 * @param show When true, show the item. Otherwise, hide it.
 * @param modifier Additional modifier to be applied to the IconButton
 */
@Composable
fun DraggableHandle(
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    show: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    IconButton(
        onClick = { },
        modifier = with(reorderableCollectionItemScope) {
            modifier
                .draggableHandle(
                    onDragStarted = { hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) },
                    onDragStopped = { hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd) }
                )
                .alpha(if (show) 1f else 0f)
        }
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_drag_handle),
            contentDescription = "Move item",
            tint = MaterialTheme.colorScheme.onSecondary,
        )
    }
}