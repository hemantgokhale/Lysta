package dev.hgokhale.lysta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt


@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
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
    val popupToTextFieldOffset = with(LocalDensity.current) { 8.dp.toPx().roundToInt() }

    if (suggestions.isNotEmpty() && showPopup) {
        Popup(
            onDismissRequest = { showPopup = false },
            offset = IntOffset(textFieldOffset.x, textFieldOffset.y - popupHeight - popupToTextFieldOffset),
            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(corner = CornerSize(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .onGloballyPositioned { popupHeight = it.size.height }
                        .focusProperties { canFocus = false }
                ) {
                    for (suggestion in suggestions) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    onSuggestionSelected(suggestion)
                                    showPopup = false
                                }
                                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(text = suggestion,
                                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f),
                                style = textStyle)
                        }
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