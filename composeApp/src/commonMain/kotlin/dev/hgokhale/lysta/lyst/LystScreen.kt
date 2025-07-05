package dev.hgokhale.lysta.lyst

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hgokhale.lysta.app.NavigationViewModel
import dev.hgokhale.lysta.getPlatform
import dev.hgokhale.lysta.utils.AutoCompleteTextField
import dev.hgokhale.lysta.utils.CollapsingTextField
import dev.hgokhale.lysta.utils.ConfigureSnackbar
import dev.hgokhale.lysta.utils.DraggableHandle
import dev.hgokhale.lysta.utils.Highlightable
import dev.hgokhale.lysta.utils.LoadingIndicator
import dev.hgokhale.lysta.utils.LystaSnackbar
import dev.hgokhale.lysta.utils.ScrollToNewItemEffect
import dev.hgokhale.lysta.utils.SwipeToDeleteItem
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Angle
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.Shape
import kotlinx.coroutines.launch
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_check_box
import lysta.composeapp.generated.resources.ic_sort
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.seconds

@Composable
fun LystScreen(
    listId: String,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier = Modifier,
    lystViewModel: LystViewModel = viewModel { LystViewModel(listID = listId) },
) {
    val items by lystViewModel.items.collectAsStateWithLifecycle()
    val listLoaded by lystViewModel.loaded.collectAsStateWithLifecycle()
    val showConfetti by lystViewModel.lastItemChecked.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var inEditMode by remember { mutableStateOf(false) }

    ConfigureSnackbar(lystViewModel, snackbarHostState)

    if (!listLoaded) {
        LoadingIndicator(modifier = modifier)
    } else {
        Scaffold(
            topBar = { LystTopBar(lystViewModel = lystViewModel, navigationViewModel = navigationViewModel) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, snackbar = { LystaSnackbar(it) }) },
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            inEditMode = !inEditMode
                        }
                ) {
                    if (items.isEmpty()) {
                        EmptyLyst()
                    } else {
                        Lyst(lystViewModel = lystViewModel, modifier = modifier)
                    }
                    AddItem(lystViewModel, inEditMode) { inEditMode = it }
                }

                if (showConfetti) {
                    ConfettiKit(
                        modifier = Modifier.fillMaxSize(),
                        parties = confetti()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LystTopBar(
    lystViewModel: LystViewModel,
    navigationViewModel: NavigationViewModel,
) {
    val showChecked by lystViewModel.showChecked.collectAsStateWithLifecycle()
    val sorted by lystViewModel.sorted.collectAsStateWithLifecycle()

    TopAppBar(
        title = { TopBarTitle(lystViewModel) },
        navigationIcon = {
            val scope = rememberCoroutineScope()
            IconButton(onClick = { scope.launch { navigationViewModel.navigateBack() } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { lystViewModel.onShowCheckedClicked() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_box),
                    contentDescription = if (showChecked) "Show checked items" else "Hide checked items",
                    tint = if (showChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                )
            }

            IconButton(onClick = { lystViewModel.onSortClicked() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_sort),
                    contentDescription = if (sorted) "Sorted" else "Not sorted",
                    tint = if (sorted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    )
}

@Composable
private fun TopBarTitle(lystViewModel: LystViewModel, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val textFieldState = rememberTextFieldState(lystViewModel.name.value)

    LaunchedEffect(Unit) {
        if (lystViewModel.focusOnTitle.value) {
            focusRequester.requestFocus()
            textFieldState.setTextAndSelectAll(lystViewModel.name.value)
        }
        snapshotFlow { textFieldState.text.toString() }.collect { lystViewModel.onNameChanged(it) }
    }

    BasicTextField(
        state = textFieldState,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        onKeyboardAction = { defaultAction -> focusManager.clearFocus(); defaultAction() },
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
    )
}

@Composable
private fun ColumnScope.EmptyLyst(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.weight(1f))
    Text(
        text = "Your list is empty.\nAdd items by tapping anywhere in the empty area or 'Add item' below.",
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
    )
    Spacer(modifier = modifier.weight(2f))
}

@Composable
private fun ColumnScope.Lyst(lystViewModel: LystViewModel, modifier: Modifier = Modifier) {
    val itemsToRender by lystViewModel.itemsToRender.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        lystViewModel.moveItem(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    ScrollToNewItemEffect(lystViewModel.newItem, lazyListState)
    LazyColumn(
        modifier = modifier.weight(1f),
        state = lazyListState
    ) {
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
}

private fun confetti() = listOf(
    Party(
        angle = Angle.TOP + 45,
        spread = 45,
        shapes = listOf(Shape.Square, Shape.Circle, Shape.Rectangle(heightRatio = 0.2f)),
        position = Position.Relative(0.0, 0.25),
        emitter = Emitter(duration = 5.seconds).perSecond(30)
    ),
    Party(
        angle = Angle.TOP - 45,
        spread = 45,
        shapes = listOf(Shape.Square, Shape.Circle, Shape.Rectangle(heightRatio = 0.2f)),
        position = Position.Relative(1.0, 0.25),
        emitter = Emitter(duration = 5.seconds).perSecond(30)
    )
)

@Composable
private fun LystItem(
    lystViewModel: LystViewModel,
    item: LystViewModel.UIItem,
    onDelete: () -> Unit,
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val isMobile = remember { getPlatform().isMobile }
    val items by lystViewModel.itemsToRender.collectAsStateWithLifecycle()
    val listIsSorted by lystViewModel.sorted.collectAsStateWithLifecycle()

    var description by remember { mutableStateOf(item.description) }
    var isHovered by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    val textStyle = LocalTextStyle.current.copy(
        color = if (item.checked) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground,
    )

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
        CollapsingTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!isFocused) {
                        lystViewModel.onItemDescriptionChanged(itemId = item.id, description = description)
                    }
                },
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        )
        if (isFocused) {
            IconButton(
                onClick = {
                    lystViewModel.onItemDescriptionChanged(itemId = item.id, description = description)
                    focusManager.clearFocus()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
        } else {
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
                DraggableHandle(
                    reorderableCollectionItemScope = reorderableCollectionItemScope,
                    show = items.size > 1
                )
            }
        }
    }
}

@Composable
private fun AddItem(lystViewModel: LystViewModel, inEditMode: Boolean, onEditModeChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.onFocusChanged { onEditModeChange(it.hasFocus) }) {
        if (inEditMode) {
            ItemEditor(
                lystViewModel = lystViewModel,
                onAddItem = { description, checked ->
                    if (description.isNotBlank()) {
                        lystViewModel.addItem(description, checked)
                    }
                },
                onCancel = { onEditModeChange(false) }
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onEditModeChange(true) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // We don't really need an IconButton here since the entire row is clickable, but using it makes this item align with all other items.
                // This is because Compose automatically adds padding around a tappable target so that it has a minimum recommended touch target size.
                IconButton(onClick = { onEditModeChange(true) }) {
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
    onCancel: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var autocompleteSuggestions: List<AutoCompleteSuggestion> by remember { mutableStateOf(listOf()) }
    val addItemAndResetTextField: () -> Unit = {
        if (text.isNotBlank()) {
            onAddItem(text, checked)
            text = ""
            autocompleteSuggestions = listOf()
            focusRequester.requestFocus()
        } else {
            onCancel()
        }
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
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            keyboardActions = KeyboardActions(onDone = { addItemAndResetTextField() }),
            singleLine = false,
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
