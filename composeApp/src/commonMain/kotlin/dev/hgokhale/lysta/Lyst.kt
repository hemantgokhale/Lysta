package dev.hgokhale.lysta

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Lyst(name: String, itemsValue: List<Item>, viewModelScope: CoroutineScope) {
    val id = Uuid.random().toString()
    private val _items: MutableStateFlow<List<Item>> = MutableStateFlow(itemsValue)

    private val _name = MutableStateFlow(name)
    val name = _name.asStateFlow()

    private val _sorted = MutableStateFlow(false)
    val sorted = _sorted.asStateFlow()

    private val _showChecked = MutableStateFlow(true)
    val showChecked = _showChecked.asStateFlow()

    val itemsToRender = combine(_items, _sorted, _showChecked) { items, sorted, showChecked ->
        items
            .filter { item -> showChecked || !item.checked.value }
            .let { list -> if (sorted) list.sortedBy { it.description.value.lowercase() } else list }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        _items.value += item
    }

    fun addItem(description: String = "", checked: Boolean = false) {
        addItem(Item(description, checked))
    }

    fun deleteItem(itemId: String): Item? {
        val item = _items.value.firstOrNull { it.id == itemId }
        if (item != null) {
            _items.value -= item
        }
        return item
    }

    override fun toString(): String = "Lyst: $name"
}