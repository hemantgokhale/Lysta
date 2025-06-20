package dev.hgokhale.lysta.lyst

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hgokhale.lysta.app.ScaffoldViewModel
import dev.hgokhale.lysta.getPlatform
import dev.hgokhale.lysta.utils.AutoCompleteTextField
import dev.hgokhale.lysta.utils.Highlightable
import dev.hgokhale.lysta.utils.LoadingIndicator
import dev.hgokhale.lysta.utils.ScrollToNewItemEffect
import dev.hgokhale.lysta.utils.SwipeToDeleteItem
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_drag_handle
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LystScreen(
    listId: String,
    scaffoldViewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier,
    lystViewModel: LystViewModel = viewModel { LystViewModel(listID = listId, scaffoldViewModel = scaffoldViewModel) }
) {
    val listLoaded by lystViewModel.loaded.collectAsStateWithLifecycle()
    if (!listLoaded) {
        LoadingIndicator(modifier = modifier)
    } else {
        ConfigureScaffold(scaffoldViewModel = scaffoldViewModel, lystViewModel = lystViewModel)
        Lyst(lystViewModel = lystViewModel, modifier = modifier)
    }
}

@Composable
private fun ConfigureScaffold(scaffoldViewModel: ScaffoldViewModel, lystViewModel: LystViewModel) {
    LaunchedEffect(Unit) {
        scaffoldViewModel.updateTopBarTitle(lystViewModel.name.value)
        scaffoldViewModel.setOnTitleChange { lystViewModel.onNameChanged(name = it) }
        scaffoldViewModel.showBackButton(true)
        scaffoldViewModel.setFabAction(null)
        lystViewModel.setTopBarActions()
    }
}

@Composable
private fun Lyst(lystViewModel: LystViewModel, modifier: Modifier = Modifier) {
    val itemsToRender by lystViewModel.itemsToRender.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        lystViewModel.moveItem(from.index, to.index)
    }

    ScrollToNewItemEffect(lystViewModel.newItem, lazyListState)

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.weight(1f), state = lazyListState) {
            items(items = itemsToRender, key = { item -> item.id }) { item ->
                ReorderableItem(state = reorderableLazyListState, key = item.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                    Surface(shadowElevation = elevation) {
                        val onDelete = { lystViewModel.deleteItem(item.id) }
                        SwipeToDeleteItem(onDelete = onDelete) {
                            Highlightable(item) { modifier ->
                                LystItem(
                                    lystViewModel = lystViewModel,
                                    item = item,
                                    onDelete = onDelete,
                                    reorderableCollectionItemScope = this,
                                    modifier = modifier
                                )
                            }
                        }
                    }
                }
            }
        }
        AddItem(lystViewModel)
    }
}

@Composable
private fun LystItem(
    lystViewModel: LystViewModel,
    item: LystViewModel.UIItem,
    onDelete: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val isMobile = remember { getPlatform().isMobile }
    val items by lystViewModel.itemsToRender.collectAsStateWithLifecycle()
    val listIsSorted by lystViewModel.sorted.collectAsStateWithLifecycle()

    var description by remember { mutableStateOf(item.description) }
    var isHovered by remember { mutableStateOf(false) }

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
            onCheckedChange = { lystViewModel.onItemCheckedChanged(itemId = item.id, isChecked = it) },
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
                        lystViewModel.onItemDescriptionChanged(itemId = item.id, description = description)
                    }
                },
            textStyle = LocalTextStyle.current.copy(
                color = if (item.checked) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground,
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
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
            IconButton(
                onClick = { },
                modifier = with(reorderableCollectionItemScope) {
                    Modifier
                        .draggableHandle()
                        .alpha(if (items.size > 1) 1f else 0f)
                }
            ) {
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
private fun AddItem(lystViewModel: LystViewModel) {
    var inEditMode by remember { mutableStateOf(false) }
    Row(modifier = Modifier.onFocusChanged { inEditMode = it.hasFocus }) {
        if (inEditMode) {
            ItemEditor(
                lystViewModel = lystViewModel,
                onAddItem = { description, checked ->
                    if (description.isNotBlank()) {
                        lystViewModel.addItem(description, checked)
                    }
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
    lystViewModel: LystViewModel,
    onAddItem: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var autocompleteSuggestions by remember { mutableStateOf(listOf<String>()) }
    val addItemAndResetTextField: () -> Unit = {
        onAddItem(text, checked)
        text = ""
        focusRequester.requestFocus()
    }

    Row(modifier = Modifier.focusGroup(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
        )
        AutoCompleteTextField(
            value = text,
            onValueChange = {
                text = it
                autocompleteSuggestions = lystViewModel.getAutocompleteSuggestions(query = it)
            },
            onSuggestionSelected = {
                lystViewModel.autocompleteSuggestionSelected(it)
                text = ""
                focusRequester.requestFocus()
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addItemAndResetTextField() }),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            suggestions = autocompleteSuggestions
        )
        IconButton(onClick = addItemAndResetTextField) {
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