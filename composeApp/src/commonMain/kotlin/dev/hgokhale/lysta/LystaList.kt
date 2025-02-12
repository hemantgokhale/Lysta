package dev.hgokhale.lysta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class LystaList(nameValue: String, itemsValue: List<Item>) {
    val name: MutableState<String> = mutableStateOf(nameValue)
    val items: MutableState<List<Item>> = mutableStateOf(itemsValue)

    class Item(descriptionValue: String, checkedValue: Boolean) {
        val description: MutableState<String> = mutableStateOf(descriptionValue)
        val checked: MutableState<Boolean> = mutableStateOf(checkedValue)
    }

}

/**
This composable function renders a [LystaList].
Unchecked items are shown first, followed by checked items.
The item description is editable in place. The checkbox is toggled when clicked.
The list is scrollable.
 */
@Composable
fun LystaList(list: LystaList) {
    val uncheckedItems = list.items.value.filter { !it.checked.value }
    val checkedItems = list.items.value.filter { it.checked.value }
    Column {
        Text(text = list.name.value)
        LazyColumn {
            items(uncheckedItems) { item ->
                LystaListItem(item = item)
            }
            items(checkedItems) { item ->
                LystaListItem(item = item)
            }
        }
    }

}

@Composable
fun LystaListItem(item: LystaList.Item) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked.value,
            onCheckedChange = { item.checked.value = it; println("Checkbox clicked. New value: $it") }
        )
        BasicTextField(
            value = item.description.value,
            onValueChange = {
                item.description.value = it
                println("Text changed. New value: $it")
            }
        )
    }
}
