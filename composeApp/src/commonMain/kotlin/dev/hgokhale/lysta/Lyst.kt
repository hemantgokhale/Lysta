package dev.hgokhale.lysta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Lyst(nameValue: String, itemsValue: List<Item>) {
    val name: MutableState<String> = mutableStateOf(nameValue)
    val items: MutableState<List<Item>> = mutableStateOf(itemsValue)

    class Item(descriptionValue: String, checkedValue: Boolean) {
        val description: MutableState<String> = mutableStateOf(descriptionValue)
        val checked: MutableState<Boolean> = mutableStateOf(checkedValue)
    }

}

/**
This composable function renders a [Lyst].
Unchecked items are shown first, followed by checked items.
The item description is editable in place. The checkbox is toggled when clicked.
The list is scrollable.
 */
@Composable
fun Lyst(list: Lyst) {
    val uncheckedItems = list.items.value.filter { !it.checked.value }
    val checkedItems = list.items.value.filter { it.checked.value }
    var isCheckedItemsExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = list.name.value, fontSize = 24.sp)
        LazyColumn {
            val uncheckedItemsTextStyle = TextStyle(color = Color.Black)
            val checkedItemsTextStyle = TextStyle(color = Color.Gray, textDecoration = TextDecoration.LineThrough)

            items(uncheckedItems) { item ->
                CompositionLocalProvider(LocalTextStyle provides uncheckedItemsTextStyle) {
                    LystItem(item = item)
                }
            }

            item { AddItem { println("Add button clicked") } }

            if (checkedItems.isNotEmpty()) {
                item {
                    CheckedItemsHeader(
                        text = "${checkedItems.size} Checked item${if (checkedItems.size > 1) "s" else ""}",
                        isExpanded = isCheckedItemsExpanded,
                        onToggle = { isCheckedItemsExpanded = !isCheckedItemsExpanded }
                    )
                }

                item {
                    AnimatedVisibility(visible = isCheckedItemsExpanded) {
                        Column {
                            checkedItems.forEach { item ->
                                CompositionLocalProvider(LocalTextStyle provides checkedItemsTextStyle) {
                                    LystItem(item = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LystItem(item: Lyst.Item) {
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
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current
        )
        IconButton(onClick = { println("Delete button clicked") }) {
            Icon(painter = rememberVectorPainter(image = Icons.Filled.Close), contentDescription = "Delete", tint = Color.Black)
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
fun AddItem(onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // We don't really need an IconButton here since the entire row is clickable, but using it makes this item aligned with all other items.
        // This is because Compose automatically adds padding around a clickable icon so that it has a minimum recommended touch target size.
        IconButton(onClick = onAdd) {
            Icon(painter = rememberVectorPainter(image = Icons.Filled.Add), contentDescription = "Add item", tint = Color.Black)
        }
        Text("Add item")
    }
}