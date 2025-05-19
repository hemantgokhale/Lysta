package dev.hgokhale.lysta

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface Highlightable {
    var showHighlight: Boolean
}

/**
 * A composable function that momentarily highlights the background of [content].
 * It is used when adding a new item or restoring an undeleted item.
 *
 * @param highlightable Determines whether the highlight animation should be shown.
 * @param modifier Modifier for customizing the appearance and layout of the component.
 * @param content Composable content to be highlighted.
 */
@Composable
fun Highlightable(
    highlightable: Highlightable,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    var showAnimation by remember { mutableStateOf(highlightable.showHighlight) }
    val backgroundColor by animateColorAsState(
        targetValue = if (showAnimation) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 200)
    )

    LaunchedEffect(Unit) {
        if (showAnimation) {
            launch {
                delay(500)
                showAnimation = false
                highlightable.showHighlight = false
            }
        }
    }

    content(modifier.background(backgroundColor))
}
