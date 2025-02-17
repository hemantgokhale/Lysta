package dev.hgokhale.lysta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Lyst(nameValue: String, itemsValue: List<Item>) {
    val items: SnapshotStateList<Item> = mutableStateListOf<Item>().also { it.addAll(itemsValue) }
    val name: MutableState<String> = mutableStateOf(nameValue)
    val sorted: MutableState<Boolean> = mutableStateOf(false)

    @OptIn(ExperimentalUuidApi::class)
    class Item(descriptionValue: String, checkedValue: Boolean) {
        val checked: MutableState<Boolean> = mutableStateOf(checkedValue)
        val description: MutableState<String> = mutableStateOf(descriptionValue)
        val id = Uuid.random().toString()
    }

    fun addItem(description: String = "", checked: Boolean = false) {
        items.add(Item(description, checked))
    }

    fun deleteItem(item: Item) {
        items.remove(item)
    }
}

/**
This composable function renders a [Lyst].
Unchecked items are shown first, followed by checked items.
The item description is editable in place. The checkbox is toggled when clicked.
The list is scrollable.
 */
@Composable
fun Lyst(list: Lyst, modifier: Modifier = Modifier) {
    val completeList = if (list.sorted.value) list.items.sortedBy { it.description.value } else list.items
    val (checkedItems, uncheckedItems) = completeList.partition { it.checked.value }

    var isCheckedItemsExpanded by remember { mutableStateOf(true) }

    LazyColumn(modifier = modifier.widthIn(max = 450.dp).padding(16.dp)) {
        item {
            BasicTextField(
                value = list.name.value,
                onValueChange = { list.name.value = it },
                textStyle = TextStyle(fontSize = 24.sp)
            )
        }

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

        val uncheckedItemsTextStyle = TextStyle(color = Color.Black)
        items(
            items = uncheckedItems,
            key = { item -> item.id }
        ) { item ->
            CompositionLocalProvider(LocalTextStyle provides uncheckedItemsTextStyle) {
                LystItem(item = item) { list.deleteItem(item) }
            }
        }

        item { AddItem(list) }

        if (checkedItems.isNotEmpty()) {
            item {
                CheckedItemsHeader(
                    text = "${checkedItems.size} Checked item${if (checkedItems.size > 1) "s" else ""}",
                    isExpanded = isCheckedItemsExpanded,
                    onToggle = { isCheckedItemsExpanded = !isCheckedItemsExpanded }
                )
            }

            if (isCheckedItemsExpanded) {
                val checkedItemsTextStyle = TextStyle(color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                items(
                    items = checkedItems,
                    key = { item -> item.id }
                ) { item ->
                    AnimatedVisibility(visible = isCheckedItemsExpanded) {
                        CompositionLocalProvider(LocalTextStyle provides checkedItemsTextStyle) {
                            LystItem(item = item) { list.deleteItem(item) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LystItem(item: Lyst.Item, onDelete: () -> Unit) {
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
            textStyle = LocalTextStyle.current
        )
        IconButton(onClick = onDelete) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Delete), contentDescription = "Delete", tint = Color.Black)
        }
    }
}

@Composable
fun CheckedItemsHeader(text: String, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .clickable { onToggle() },
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
fun AddItem(list: Lyst) {
    var inEditMode by remember { mutableStateOf(false) }

    if (inEditMode) {
        ItemEditor("", false) { description, checked ->
            if (description.isNotBlank()) {
                list.addItem(description, checked)
            }
            inEditMode = false
        }
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
            Text("Add item")
        }
    }
}

@Composable
private fun ItemEditor(descriptionToEdit: String, checkedToEdit: Boolean, onDone: (String, Boolean) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var description by remember { mutableStateOf(descriptionToEdit) }
    var checked by remember { mutableStateOf(checkedToEdit) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
        )
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current
        )
        IconButton(onClick = { onDone(description, checked) }) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Check), contentDescription = "Done", tint = Color.Black)
        }
        IconButton(onClick = { onDone(descriptionToEdit, checkedToEdit) }) {
            Icon(painter = rememberVectorPainter(image = Icons.Default.Clear), contentDescription = "Cancel", tint = Color.Black)
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}