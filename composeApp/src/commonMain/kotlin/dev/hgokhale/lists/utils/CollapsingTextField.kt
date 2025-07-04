package dev.hgokhale.lists.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints

/**
 * A VisualTransformation that truncates text with an ellipsis when the field is not focused
 * and the text would overflow the available width.
 *
 * This class now correctly accepts a TextMeasurer from a Composable context.
 */
private class EllipsisOnUnfocusTransformation(
    private val textMeasurer: TextMeasurer,
    private val isFocused: Boolean,
    private val textStyle: TextStyle,
    private val constraints: Constraints,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        // If the field is focused or empty, no transformation is needed.
        if (isFocused || text.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // First, create the potentially transformed text by handling newlines.
        var finalText = if (text.text.contains('\n')) {
            AnnotatedString(text.text.replace(Regex("\\n"), " ").trim())
        } else {
            text
        }

        // Second, truncate the text if it overflows.
        val textWidth = textMeasurer.measure(finalText, style = textStyle).size.width
        val availableWidth = constraints.maxWidth

        if (textWidth > availableWidth) {
            val ellipsis = "..."
            val ellipsisWidth = textMeasurer.measure(ellipsis, style = textStyle).size.width
            var truncatedEndIndex = 0
            // Binary search to find the optimal truncation point
            var low = 0
            var high = finalText.length
            while (low <= high) {
                val mid = (low + high) / 2
                val substring = finalText.substring(0, mid)
                val substringWidth = textMeasurer.measure(substring, style = textStyle).size.width
                if (substringWidth + ellipsisWidth <= availableWidth) {
                    // This one fits, it's a candidate. Try for a longer substring.
                    truncatedEndIndex = mid
                    low = mid + 1
                } else {
                    // This one is too long, try for a shorter substring.
                    high = mid - 1
                }
            }
            finalText = AnnotatedString(finalText.substring(0, truncatedEndIndex) + ellipsis)
        }

        // If the final text is identical to the original, no custom mapping is needed.
        if (finalText.text == text.text) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // The text has changed, so we need a custom OffsetMapping.
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Clamp the offset to the valid range of the transformed text.
                // This prevents the app from crashing if the cursor was originally
                // positioned past the end of the new, shorter text.
                return offset.coerceAtMost(finalText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // This maps an offset from the transformed text back to the original.
                // Because our transformation (truncation) is lossy and always shortens the text,
                // a perfect mapping is impossible. However, returning the offset directly is
                // a safe and valid approach here because the user cannot interact with the
                // text field (e.g., move the cursor) while it's unfocused and this transformation is active.
                return offset
            }
        }

        return TransformedText(finalText, offsetMapping)
    }
}

/**
 * A TextField that is multi-line when focused but displays as a single-line
 * field with an ellipsis for overflow when not focused.
 *
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun CollapsingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() },
) {
    var isFocused by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val transformation = remember(textMeasurer, isFocused, constraints) {
            EllipsisOnUnfocusTransformation(textMeasurer, isFocused, textStyle, constraints)
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = !isFocused,
            maxLines = if (isFocused) Int.MAX_VALUE else 1,
            visualTransformation = transformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = decorationBox
        )
    }
}