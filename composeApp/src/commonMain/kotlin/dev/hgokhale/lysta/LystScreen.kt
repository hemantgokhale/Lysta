package dev.hgokhale.lysta

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource

@Composable
fun LystScreen(listId: String, modifier: Modifier = Modifier, viewModel: LystViewModel) {
    LaunchedEffect(listId) { viewModel.loadList(listId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    (uiState as? LystViewModel.UIState.Lyst)
        ?.lyst
        ?.let { Lyst(list = it, modifier = modifier, viewModel = viewModel) }
        ?: LoadingIndicator(modifier = modifier)
}

@Composable
private fun Lyst(list: Lyst, modifier: Modifier = Modifier, viewModel: LystViewModel) {
    val itemsToRender by list.itemsToRender.collectAsStateWithLifecycle()
    Column(modifier = modifier) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = itemsToRender, key = { item -> item.id }) { item ->
                DismissibleItem(viewModel = viewModel, list = list, item = item)
            }
        }

        AddItem(list)
    }
}

@Composable
private fun DismissibleItem(viewModel: LystViewModel, list: Lyst, item: Lyst.Item) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                viewModel.deleteItem(list.id, item.id)
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Row(
                modifier = Modifier.fillMaxSize().background(color),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.padding(16.dp), tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        LystItem(list = list, item = item)
    }
}

@Composable
private fun LystItem(list: Lyst, item: Lyst.Item) {
    val focusManager = LocalFocusManager.current
    var description by remember { mutableStateOf(item.description) }
    val colorScheme = MaterialTheme.colorScheme
    val textStyle = remember(item.checked) {
        if (item.checked)
            TextStyle(color = colorScheme.onBackground, textDecoration = TextDecoration.LineThrough)
        else
            TextStyle(color = colorScheme.onBackground)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { list.onItemCheckedChanged(itemId = item.id, isChecked = it) },
        )
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        list.onItemDescriptionChanged(itemId = item.id, description = description)
                    }
                },
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )
        IconButton(onClick = { }) {
            Icon(
                painter = painterResource(Res.drawable.ic_drag_handle),
                contentDescription = "Move item",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun AddItem(list: Lyst) {
    var inEditMode by remember { mutableStateOf(false) }
    if (inEditMode) {
        ItemEditor(
            textToEdit = "",
            checkedToEdit = false,
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
            Text("Add item", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun ItemEditor(
    textToEdit: String,
    checkedToEdit: Boolean,
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
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
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