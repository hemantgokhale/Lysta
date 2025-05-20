package dev.hgokhale.lysta

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Lyst(name: String, itemsValue: List<Item>, val viewModelScope: CoroutineScope) : Highlightable {
    data class Item(
        val description: String,
        val checked: Boolean,
        override var showHighlight: Boolean = false,
    ) : Highlightable {
        val id: String = Uuid.random().toString()
    }

    val id = Uuid.random().toString()
    private val _items: MutableStateFlow<List<Item>> = MutableStateFlow(itemsValue)

    private val _name = MutableStateFlow(name)
    val name = _name.asStateFlow()

    private val _sorted = MutableStateFlow(false)
    val sorted = _sorted.asStateFlow()

    private val _showChecked = MutableStateFlow(true)
    val showChecked = _showChecked.asStateFlow()

    private val _newItem = MutableSharedFlow<Int>() // index of a newly added item
    val newItem: SharedFlow<Int> get() = _newItem

    val itemsToRender = combine(_items, _sorted, _showChecked) { items, sorted, showChecked ->
        items
            .filter { item -> showChecked || !item.checked }
            .let { list -> if (sorted) list.sortedBy { it.description.lowercase() } else list }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var deletedItem: Pair<Int, Item>? = null // first = index, second = item
    override var showHighlight = false

    fun onShowCheckedClicked() {
        _showChecked.value = !showChecked.value
    }

    fun onSortClicked() {
        _sorted.value = !sorted.value
    }

    fun onNameChanged(name: String) {
        _name.value = name
    }

    fun addItem(description: String = "", checked: Boolean = false) : Item {
        val item = Item(description, checked, showHighlight = true)
        _items.value += item
        publishNewItemNotification(item)
        return item
    }

    private fun publishNewItemNotification(item: Item) {
        viewModelScope.launch {
            val index = itemsToRender.value.indexOf(item)
            if (index != -1) _newItem.emit(index)
        }
    }

    fun deleteItem(itemId: String): Item? {
        val index = _items.value.indexOfFirst { it.id == itemId }
        return if (index != -1) {
            val itemToDelete = _items.value[index]
            _items.value -= itemToDelete
            deletedItem = Pair(index, itemToDelete)
            itemToDelete
        } else {
            null
        }
    }

    fun undeleteItem() {
        deletedItem?.let { (index, item) ->
            _items.value = _items.value.toMutableList().apply { add(index, item.apply { showHighlight = true }) }
            publishNewItemNotification(item)
            deletedItem = null
        }
    }

    fun onItemDescriptionChanged(itemId: String, description: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(description = description)
            } else {
                item
            }
        }
    }

    fun onItemCheckedChanged(itemId: String, isChecked: Boolean) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(checked = isChecked)
            } else {
                item
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        if (from != to && from in _items.value.indices && to in _items.value.indices) {
            val mutableList = _items.value.toMutableList()
            mutableList.add(to, mutableList.removeAt(from))
            _items.value = mutableList
        }
    }

    fun getAutocompleteSuggestions(query: String): List<String> =
        if (query.isEmpty()) {
            emptyList()
        } else {
            _items.value
                .filter { it.description.startsWith(query, ignoreCase = true) }
                .map { it.description }
        }

    fun autocompleteSuggestionSelected(suggestion: String) {
        _items.value = _items.value.map { item ->
            if (item.description == suggestion) {
                item.copy(checked = false)
            } else {
                item
            }
        }
    }

    override fun toString(): String = "Lyst: $name"
}