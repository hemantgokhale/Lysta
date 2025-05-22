package dev.hgokhale.lysta

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LystScreen(listId: String, modifier: Modifier = Modifier, viewModel: LystaViewModel) {
    LaunchedEffect(listId) { viewModel.loadList(listId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    (uiState as? LystaViewModel.UIState.Lyst)
        ?.lyst
        ?.let { Lyst(viewModel = viewModel, list = it, modifier = modifier) }
        ?: LoadingIndicator(modifier = modifier)
}

@Composable
private fun Lyst(viewModel: LystaViewModel, list: Lyst, modifier: Modifier = Modifier) {
    val itemsToRender by list.itemsToRender.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list.moveItem(from.index, to.index)
    }

    ScrollToNewItemEffect(list.newItem, lazyListState)

    Column(modifier = modifier) {
        LazyColumn(modifier = Modifier.weight(1f), state = lazyListState) {
            items(items = itemsToRender, key = { item -> item.id }) { item ->
                ReorderableItem(state = reorderableLazyListState, key = item.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                    Surface(shadowElevation = elevation) {
                        val onDelete = { viewModel.deleteItem(list.id, item.id) }
                        SwipeToDeleteItem(onDelete = onDelete) {
                            Highlightable(item) { modifier ->
                                LystItem(list = list, item = item, onDelete = onDelete, reorderableCollectionItemScope = this, modifier = modifier)
                            }
                        }
                    }
                }
            }
        }
        AddItem(list)
    }
}

@Composable
private fun LystItem(
    list: Lyst,
    item: Lyst.Item,
    onDelete: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var description by remember { mutableStateOf(item.description) }
    val listIsSorted by list.sorted.collectAsStateWithLifecycle()
    var isHovered by remember { mutableStateOf(false) }
    val isMobile = remember { getPlatform().isMobile }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.pointerInput(Unit) {
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
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { list.onItemCheckedChanged(itemId = item.id, isChecked = it) },
            colors = CheckboxColors(
                checkedCheckmarkColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedCheckmarkColor = MaterialTheme.colorScheme.background,
                checkedBoxColor = MaterialTheme.colorScheme.background,
                uncheckedBoxColor = MaterialTheme.colorScheme.background,
                disabledCheckedBoxColor = MaterialTheme.colorScheme.onSecondary,
                disabledUncheckedBoxColor = MaterialTheme.colorScheme.onSecondary,
                disabledIndeterminateBoxColor = MaterialTheme.colorScheme.onSecondary,
                checkedBorderColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedBorderColor = MaterialTheme.colorScheme.onSecondary,
                disabledBorderColor = MaterialTheme.colorScheme.onSecondary,
                disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSecondary,
                disabledIndeterminateBorderColor = MaterialTheme.colorScheme.onSecondary,
            ),
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
            textStyle = LocalTextStyle.current.copy(
                color = if (item.checked) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        )
        if (!isMobile && isHovered) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
        if (!listIsSorted) {
            IconButton(onClick = { }, modifier = with(reorderableCollectionItemScope) { Modifier.draggableHandle() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_drag_handle),
                    contentDescription = "Move item",
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@Composable
private fun AddItem(list: Lyst) {
    var inEditMode by remember { mutableStateOf(false) }
    Row(modifier = Modifier.onFocusChanged { inEditMode = it.hasFocus }) {
        if (inEditMode) {
            ItemEditor(
                list = list,
                onDone = { description, checked ->
                    if (description.isNotBlank()) {
                        list.addItem(description, checked)
                    }
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
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add item", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text("Add item", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun ItemEditor(
    list: Lyst,
    onDone: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var autocompleteSuggestions by remember { mutableStateOf(listOf<String>()) }

    Row(modifier = Modifier.focusGroup(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
        )
        AutoCompleteTextField(
            value = text,
            onValueChange = {
                text = it
                autocompleteSuggestions = list.getAutocompleteSuggestions(query = it)
            },
            onSuggestionSelected = {
                list.autocompleteSuggestionSelected(it)
                onCancel()
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone(text, checked) }),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            suggestions = autocompleteSuggestions
        )
        IconButton(onClick = { onDone(text, checked) }) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onCancel) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onBackground)
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}