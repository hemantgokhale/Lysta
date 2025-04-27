package dev.hgokhale.lysta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt


@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    cursorBrush: Brush,
    suggestions: List<String>,
) {
    var popupHeight by remember { mutableStateOf(0) } // in pixels
    var showPopup by remember { mutableStateOf(false) }
    var textFieldOffset by remember { mutableStateOf(IntOffset.Zero) }

    if (suggestions.isNotEmpty() && showPopup) {
        Popup(
            onDismissRequest = { showPopup = false },
            offset = IntOffset(textFieldOffset.x, textFieldOffset.y - popupHeight),
            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
        ) {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.onGloballyPositioned { popupHeight = it.size.height }) {
                    for (suggestion in suggestions) {
                        Text(
                            text = suggestion,
                            style = textStyle,
                            modifier = Modifier
                                .clickable {
                                    onValueChange(suggestion)
                                    showPopup = false
                                }
                        )
                    }
                }
            }
        }
    }
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it); showPopup = true },
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInParent()
                textFieldOffset = IntOffset(x = position.x.roundToInt(), y = position.y.roundToInt())
            },
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        cursorBrush = cursorBrush,
    )
}