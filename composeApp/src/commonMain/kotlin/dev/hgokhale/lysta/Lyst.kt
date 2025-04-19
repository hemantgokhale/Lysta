package dev.hgokhale.lysta

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Lyst(name: String, itemsValue: List<Item>) {
    val id = Uuid.random().toString()
    private val _items: SnapshotStateList<Item> = mutableStateListOf<Item>().also { it.addAll(itemsValue) }
    val items: List<Item> get() = _items

    private val _name = MutableStateFlow(name)
    val name = _name.asStateFlow()

    private val _sorted = MutableStateFlow(false)
    val sorted = _sorted.asStateFlow()

    private val _showChecked = MutableStateFlow(true)
    val showChecked = _showChecked.asStateFlow()

    fun onShowCheckedClicked() {
        _showChecked.value = !showChecked.value
    }

    fun onSortClicked() {
        _sorted.value = !sorted.value
    }

    fun onNameChanged(name: String) {
        _name.value = name
    }

    class Item(descriptionValue: String, checkedValue: Boolean) {
        val checked: MutableState<Boolean> = mutableStateOf(checkedValue)
        val description: MutableState<String> = mutableStateOf(descriptionValue)
        val id = Uuid.random().toString()
    }

    fun addItem(item: Item) {
        _items.add(item)
    }

    fun addItem(description: String = "", checked: Boolean = false) {
        _items.add(Item(description, checked))
    }

    fun deleteItem(item: Item) {
        _items.remove(item)
    }

    override fun toString(): String = "Lyst: $name"
}