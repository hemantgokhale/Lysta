package dev.hgokhale.lysta

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Lyst(name: String, itemsValue: List<Item>) {
    @OptIn(ExperimentalUuidApi::class)
    val id = Uuid.random().toString()
    private val _items: SnapshotStateList<Item> = mutableStateListOf<Item>().also { it.addAll(itemsValue) }
    val items: List<Item> get() = _items

    val name: MutableState<String> = mutableStateOf(name)
    val sorted: MutableState<Boolean> = mutableStateOf(false)

    @OptIn(ExperimentalUuidApi::class)
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