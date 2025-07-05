package dev.hgokhale.lists.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

@Composable
fun DraggableFAB(onClick: () -> Unit) {
    var fabOffset by remember { mutableStateOf(IntOffset.Zero) }
    var minOffset by remember { mutableStateOf(IntOffset.Zero) }

    SmallFloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .offset { fabOffset }
            .onGloballyPositioned {
                if (minOffset == IntOffset.Zero) { // Need to do this only once at the beginning.
                    val mySize = it.size
                    val parentSize = it.parentLayoutCoordinates?.size ?: IntSize.Zero
                    val positionInParent = IntOffset(x = it.positionInParent().x.roundToInt(), y = it.positionInParent().y.roundToInt())
                    val xPadding = parentSize.width - (positionInParent.x + mySize.width)
                    val yPadding = parentSize.height - (positionInParent.y + mySize.height)
                    minOffset = IntOffset(x = xPadding - positionInParent.x, y = yPadding - positionInParent.y)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // Clamp the FAB offset within boundaries of the parent box
                    val offsetX = (fabOffset.x + dragAmount.x.toInt()).coerceIn(minOffset.x, 0)
                    val offsetY = (fabOffset.y + dragAmount.y.toInt()).coerceIn(minOffset.y, 0)
                    fabOffset = IntOffset(offsetX, offsetY)
                }
            },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add item")
    }

}