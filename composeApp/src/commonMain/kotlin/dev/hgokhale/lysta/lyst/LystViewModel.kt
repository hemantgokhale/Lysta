package dev.hgokhale.lysta.lyst

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.ScaffoldViewModel
import dev.hgokhale.lysta.app.SnackbarEvent
import dev.hgokhale.lysta.app.SnackbarEventBus
import dev.hgokhale.lysta.app.TopBarAction
import dev.hgokhale.lysta.home.TestRepository
import dev.hgokhale.lysta.utils.Highlightable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_check_box
import lysta.composeapp.generated.resources.ic_sort
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Lyst(
    val name: String,
    val isSorted: Boolean = false,
    val showChecked: Boolean = true,
    val items: List<Item> = emptyList(),
    val id: String = Uuid.random().toString(),
) {
    data class Item(
        val description: String,
        val checked: Boolean = false,
        val id: String = Uuid.random().toString(),
    )
}

@OptIn(ExperimentalUuidApi::class)
class LystViewModel(val listID: String, val scaffoldViewModel: ScaffoldViewModel) : ViewModel() {
    data class UIItem(
        val listItem: Lyst.Item,
        override var showHighlight: Boolean = false,
    ) : Highlightable {
        val id: String get() = listItem.id
        val description: String get() = listItem.description
        val checked: Boolean get() = listItem.checked
    }

    private val _loaded = MutableStateFlow(false)
    val loaded = _loaded.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _sorted = MutableStateFlow(false)
    val sorted = _sorted.asStateFlow()

    private val _showChecked = MutableStateFlow(true)
    val showChecked = _showChecked.asStateFlow()

    private val _items: MutableStateFlow<List<UIItem>> = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            //LystRepository.loadList
            val list = TestRepository.lists.first { it.id == listID } // TODO handle the case of not found

            _items.value = list.items.map { UIItem(it) }
            _name.value = list.name
            _sorted.value = list.isSorted
            _showChecked.value = list.showChecked
            _loaded.value = true
        }
    }

    private val _newItem = MutableSharedFlow<Int>() // index of a newly added item
    val newItem: SharedFlow<Int> get() = _newItem

    val itemsToRender = combine(_items, _sorted, _showChecked) { items, sorted, showChecked ->
        items
            .filter { item -> showChecked || !item.checked }
            .let { list -> if (sorted) list.sortedBy { it.description.lowercase() } else list }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var deletedItem: Pair<Int, UIItem>? = null // first = index, second = item

    fun setTopBarActions() {
        scaffoldViewModel.topBarActions.value = listOf(
            TopBarAction(
                contentDescription = if (showChecked.value) "Show checked items" else "Hide checked items",
                icon = Res.drawable.ic_check_box,
                isOn = showChecked.value,
                onClick = ::onShowCheckedClicked,
            ),
            TopBarAction(
                contentDescription = if (sorted.value) "Sorted" else "Not sorted",
                icon = Res.drawable.ic_sort,
                isOn = sorted.value,
                onClick = ::onSortClicked,
            )
        )
    }

    fun onShowCheckedClicked() {
        _showChecked.value = !showChecked.value
        setTopBarActions()
        // LystRepository.updateShowChecked
    }

    fun onSortClicked() {
        _sorted.value = !sorted.value
        setTopBarActions()
        // LystRepository.updateSorted
    }

    fun onNameChanged(name: String) {
        _name.value = name
        scaffoldViewModel.topBarTitle.value = name
        // LystRepository.updateName
    }

    fun addItem(description: String = "", checked: Boolean = false): UIItem {
        val item = Lyst.Item(description = description, checked = checked)
        // LystRepository.newItem
        val screenItem = UIItem(listItem = item, showHighlight = true)
        _items.value += screenItem
        publishNewItemNotification(screenItem)
        return screenItem
    }

    private fun publishNewItemNotification(item: UIItem) {
        viewModelScope.launch {
            val index = itemsToRender.value.indexOf(item)
            if (index != -1) _newItem.emit(index)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            val index = _items.value.indexOfFirst { it.id == itemId }
            if (index != -1) {
                val itemToDelete = _items.value[index]
                _items.value -= itemToDelete
                deletedItem = Pair(index, itemToDelete)
                // LystRepository.deleteItem

                SnackbarEventBus.send(
                    SnackbarEvent(
                        message = "Deleted: ${itemToDelete.description}",
                        actionLabel = "Undo",
                        action = { undeleteItem() }
                    )
                )
            }
        }
    }

    fun undeleteItem() {
        deletedItem?.let { (index, item) ->
            _items.value = _items.value.toMutableList().apply { add(index, item.apply { showHighlight = true }) }
            publishNewItemNotification(item)
            deletedItem = null
            // LystRepository.restoreItem
        }
    }

    fun onItemDescriptionChanged(itemId: String, description: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(listItem = item.listItem.copy(description = description))
                // LystRepository.updateDescription
            } else {
                item
            }
        }
    }

    fun onItemCheckedChanged(itemId: String, isChecked: Boolean) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(listItem = item.listItem.copy(checked = isChecked))
                // LystRepository.updateIsChecked
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
            // LystRepository.moveItem
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
            if (item.description == suggestion && item.checked) {
                item.copy(listItem = item.listItem.copy(checked = false))
                // LystRepository.updateIsChecked
            } else {
                item
            }
        }
    }

    override fun toString(): String = "List: $name"
}