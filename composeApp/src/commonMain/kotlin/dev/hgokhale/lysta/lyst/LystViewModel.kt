package dev.hgokhale.lysta.lyst

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.ScaffoldViewModel
import dev.hgokhale.lysta.app.TopBarAction
import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.repository.getRepository
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

    private val _listNotFound = MutableStateFlow(false)
    val listNotFound = _listNotFound.asStateFlow() // TODO this can happen for a web app if the user types in an invalid list id

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _sorted = MutableStateFlow(false)
    val sorted = _sorted.asStateFlow()

    private val _showChecked = MutableStateFlow(true)
    val showChecked = _showChecked.asStateFlow()

    private val _items: MutableStateFlow<List<UIItem>> = MutableStateFlow(emptyList())
    private val repository = getRepository()

    init {
        viewModelScope.launch {
            repository
                .getList(listID)
                ?.let { list ->
                    _items.value = list.items.map { UIItem(it) }
                    _name.value = list.name
                    _sorted.value = list.isSorted
                    _showChecked.value = list.showChecked
                }
                ?: run {
                    _listNotFound.value = true
                }
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
        scaffoldViewModel.setTopBarActions(
            listOf(
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
        )
    }

    fun onShowCheckedClicked() {
        _showChecked.value = !showChecked.value
        repository.updateShowChecked(listID, showChecked.value)
        setTopBarActions()
    }

    fun onSortClicked() {
        _sorted.value = !sorted.value
        repository.updateSorted(listID, sorted.value)
        setTopBarActions()
    }

    fun onNameChanged(name: String) {
        _name.value = name
        repository.updateName(listID, name)
        scaffoldViewModel.updateTopBarTitle(name)
    }

    fun addItem(description: String = "", checked: Boolean = false): UIItem {
        val item = Lyst.Item(description = description, checked = checked)
        repository.addItem(listID, item)
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
                repository.deleteItem(listID, itemId)
                deletedItem = Pair(index, itemToDelete)

                scaffoldViewModel.showSnackbar(
                    message = "Deleted: ${itemToDelete.description}",
                    actionLabel = "Undo",
                    action = { undeleteItem() }
                )
            }
        }
    }

    fun undeleteItem() {
        deletedItem?.let { (index, item) ->
            _items.value = _items.value.toMutableList().apply { add(index, item.apply { showHighlight = true }) }
            repository.restoreItem(listID, Lyst.Item(description = item.description, checked = item.checked, id = item.id), index)
            publishNewItemNotification(item)
            deletedItem = null
        }
    }

    fun onItemDescriptionChanged(itemId: String, description: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item
                    .copy(listItem = item.listItem.copy(description = description))
                    .also { repository.updateItemDescription(listID, itemId, description) }
            } else {
                item
            }
        }
    }

    fun onItemCheckedChanged(itemId: String, isChecked: Boolean) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item
                    .copy(listItem = item.listItem.copy(checked = isChecked))
                    .also { repository.updateItemChecked(listID, itemId, isChecked) }
            } else {
                item
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        if (from != to && from in itemsToRender.value.indices && to in itemsToRender.value.indices) {
            val fromItem = itemsToRender.value[from]
            val toItem = itemsToRender.value[to]

            // The actual index may be different from the supplied index if checked items are hidden
            val actualFromIndex = _items.value.indexOfFirst { it.id == fromItem.id }
            val actualToIndex = _items.value.indexOfFirst { it.id == toItem.id }

            val mutableList = _items.value.toMutableList()
            mutableList.add(actualToIndex, mutableList.removeAt(actualFromIndex))
            _items.value = mutableList
            repository.moveItem(listID, fromItem.id, actualFromIndex, actualToIndex)
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
                item
                    .copy(listItem = item.listItem.copy(checked = false))
                    .also { repository.updateItemChecked(listID, item.id, checked = false) }
            } else {
                item
            }
        }
    }

    override fun toString(): String = "List: $name"
}