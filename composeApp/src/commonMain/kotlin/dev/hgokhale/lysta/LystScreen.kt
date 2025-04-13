package dev.hgokhale.lysta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun LystScreen(listId: String, modifier: Modifier = Modifier, viewModel: LystViewModel) {
    LaunchedEffect(listId) { viewModel.loadList(listId) }
    val uiState by viewModel.uiState.collectAsState()

    (uiState as? LystViewModel.UIState.Lyst)
        ?.lyst
        ?.let { Lyst(list = it, modifier = modifier, viewModel = viewModel) }
        ?: LoadingIndicator(modifier = modifier)
}

/**
This composable function renders a [Lyst].
Unchecked items are shown first, followed by checked items.
The item description is editable in place. The checkbox is toggled when clicked.
The list is scrollable.
 */
@Composable
private fun Lyst(list: Lyst, modifier: Modifier = Modifier, viewModel: LystViewModel) {
    val completeList = if (list.sorted.value) list.items.sortedBy { it.description.value } else list.items
    val (checkedItems, uncheckedItems) = completeList.partition { it.checked.value }

    var isCheckedItemsExpanded by remember { mutableStateOf(true) }
    val uncheckedItemsTextStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    val checkedItemsTextStyle = TextStyle(color = Color.Gray, textDecoration = TextDecoration.LineThrough)

    LazyColumn(modifier = modifier) {

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Text("Sort ")
                Switch(
                    checked = list.sorted.value,
                    onCheckedChange = { list.sorted.value = it },
                    modifier = Modifier.scale(0.75f)
                )
            }
        }

        items(
            items = uncheckedItems,
            key = { item -> item.id }
        ) { item ->
            LystItem(item = item, textStyle = uncheckedItemsTextStyle) { viewModel.deleteItem(list.id, item.id) }
        }

        item {
            AddItem(list, textStyle = uncheckedItemsTextStyle)
        }

        if (checkedItems.isNotEmpty()) {
            item {
                CheckedItemsHeader(
                    text = "${checkedItems.size} Checked item${if (checkedItems.size > 1) "s" else ""}",
                    isExpanded = isCheckedItemsExpanded,
                    onToggle = { isCheckedItemsExpanded = !isCheckedItemsExpanded }
                )
            }

            if (isCheckedItemsExpanded) {
                items(
                    items = checkedItems,
                    key = { item -> item.id }
                ) { item ->
                    AnimatedVisibility(visible = isCheckedItemsExpanded) {
                        LystItem(item = item, textStyle = checkedItemsTextStyle) { viewModel.deleteItem(list.id, item.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LystItem(item: Lyst.Item, textStyle: TextStyle, onDelete: () -> Unit) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.checked.value,
            onCheckedChange = { item.checked.value = it },
        )
        BasicTextField(
            value = item.description.value,
            onValueChange = { item.description.value = it },
            modifier = Modifier
                .weight(1f),
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )
        IconButton(onClick = onDelete) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Delete), contentDescription = "Delete", tint = Color.Black)
        }
    }
}

@Composable
private fun CheckedItemsHeader(text: String, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Expand/Collapse",
            modifier = Modifier.rotate(rotationAngle)
        )
        Text(text = text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AddItem(list: Lyst, textStyle: TextStyle) {
    var inEditMode by remember { mutableStateOf(false) }

    if (inEditMode) {
        ItemEditor(
            textToEdit = "",
            checkedToEdit = false,
            textStyle = textStyle,
            onDone = { description, checked ->
                if (description.isNotBlank()) list.addItem(description, checked)
                inEditMode = false
            },
            onCancel = { inEditMode = false }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { inEditMode = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // We don't really need an IconButton here since the entire row is clickable, but using it makes this item align with all other items.
            // This is because Compose automatically adds padding around a tappable target so that it has a minimum recommended touch target size.
            IconButton(onClick = { inEditMode = true }) {
                Icon(painter = rememberVectorPainter(image = Icons.Filled.Add), contentDescription = "Add item", tint = Color.Black)
            }
            Text("Add item", style = textStyle)
        }
    }
}

@Composable
private fun ItemEditor(
    textToEdit: String,
    checkedToEdit: Boolean,
    textStyle: TextStyle,
    onDone: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf(textToEdit) }
    var checked by remember { mutableStateOf(checkedToEdit) }

    Row(modifier = Modifier.fillMaxWidth().focusGroup(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
        )
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone(text, checked) }),
            singleLine = true
        )
        IconButton(onClick = { onDone(text, checked) }) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Check), contentDescription = "Done", tint = Color.Black)
        }
        IconButton(onClick = onCancel) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Clear), contentDescription = "Cancel", tint = Color.Black)
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            focusRequester.captureFocus()
        }
    }
}