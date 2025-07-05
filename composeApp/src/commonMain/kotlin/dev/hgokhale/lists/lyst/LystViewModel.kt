package dev.hgokhale.lists.lyst

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lists.model.Lyst
import dev.hgokhale.lists.repository.getRepository
import dev.hgokhale.lists.utils.Highlightable
import dev.hgokhale.lists.utils.SnackbarState
import dev.hgokhale.lists.utils.SnackbarStateImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi


data class AutoCompleteSuggestion(val text: String, val checked: Boolean)

@OptIn(ExperimentalUuidApi::class)
class LystViewModel(val listID: String) : ViewModel(), SnackbarState by SnackbarStateImpl() {
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
    val items = _items.asStateFlow()

    private val repository = getRepository()

    private val _lastItemChecked = MutableStateFlow(false)
    val lastItemChecked = _lastItemChecked.asStateFlow()

    private val _focusOnTitle = MutableStateFlow(false)
    val focusOnTitle = _focusOnTitle.asStateFlow()

    init {
        viewModelScope.launch {
            repository
                .getList(listID)
                ?.let { list ->
                    _items.value = list.items.map { UIItem(it) }
                    _name.value = list.name
                    _sorted.value = list.isSorted
                    _showChecked.value = list.showChecked
                    _focusOnTitle.value = list.isNew
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

    fun onShowCheckedClicked() {
        _showChecked.value = !showChecked.value
        repository.updateShowChecked(listID, showChecked.value)
    }

    fun onSortClicked() {
        _sorted.value = !sorted.value
        repository.updateSorted(listID, sorted.value)
    }

    fun onNameChanged(name: String) {
        _name.value = name
        repository.updateName(listID, name)
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

                showSnackbar(
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

        _lastItemChecked.value = _items.value.all { it.checked }
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

    fun getAutocompleteSuggestions(query: String): List<AutoCompleteSuggestion> =
        if (query.isEmpty()) {
            emptyList()
        } else {
            _items.value
                .filter { it.description.startsWith(query, ignoreCase = true) }
                .map { AutoCompleteSuggestion(text = it.description, checked = it.checked) }
        }

    fun autocompleteSuggestionSelected(suggestion: String) {
        var itemInQuestion: UIItem? = null
        _items.value = _items.value.map { item ->
            if (item.description == suggestion) {
                item
                    .copy(listItem = item.listItem.copy(checked = false), showHighlight = true)
                    .also {
                        repository.updateItemChecked(listID, it.id, checked = false)
                        itemInQuestion = it
                    }
            } else {
                item
            }
        }
        // It is important that the new item notification is published after items are updated.
        // Cannot do it inside `also`
        itemInQuestion?.let { publishNewItemNotification(it) }
    }

    override fun toString(): String = "List: $name"
}
