package dev.hgokhale.lysta.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.NavigationDestination
import dev.hgokhale.lysta.app.NavigationEvent
import dev.hgokhale.lysta.app.NavigationEventBus
import dev.hgokhale.lysta.app.SnackbarEvent
import dev.hgokhale.lysta.app.SnackbarEventBus
import dev.hgokhale.lysta.lyst.Lyst
import dev.hgokhale.lysta.utils.Highlightable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    data class UIItem(val id: String, val name: String, override var showHighlight: Boolean = false) : Highlightable

    private val _loaded = MutableStateFlow(false)
    val loaded = _loaded.asStateFlow()

    private val _lists: MutableStateFlow<List<UIItem>> = MutableStateFlow(emptyList())
    val lists: StateFlow<List<UIItem>> = _lists.asStateFlow()

    init {
        viewModelScope.launch {
            // Repository.loadLists

            _lists.value = TestRepository.lists.map { UIItem(it.id, it.name) }
            _loaded.value = true
        }
    }

    private val _newItem = MutableSharedFlow<Int>() // index of a newly added item
    val newItem: SharedFlow<Int> get() = _newItem

    private var deletedList: Pair<Int, UIItem>? = null // first = index, second = list

    fun moveList(from: Int, to: Int) {
        if (from != to && from in _lists.value.indices && to in _lists.value.indices) {
            val mutableList = _lists.value.toMutableList()
            mutableList.add(to, mutableList.removeAt(from))
            _lists.value = mutableList
        }
    }

    fun createList(): String {
        val lyst = Lyst(name = "New list")
        val newItem = UIItem(lyst.id, lyst.name)
        // LystRepository.newList
        _lists.value += newItem
        publishNewItemNotification(newItem)
        viewModelScope.launch {
            NavigationEventBus.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(lyst.id)))
        }
        return lyst.id
    }

    fun onListClicked(id: String) {
        viewModelScope.launch { NavigationEventBus.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(id))) }
    }

    fun deleteList(id: String) {
        viewModelScope.launch {
            val index = _lists.value.indexOfFirst { it.id == id }
            if (index != -1) {
                val listToDelete = _lists.value[index]
                _lists.value -= listToDelete
                deletedList = Pair(index, listToDelete)
                SnackbarEventBus.send(
                    SnackbarEvent(
                        message = "Deleted: ${listToDelete.name}",
                        actionLabel = "Undo",
                        action = { undeleteList() }
                    )
                )
                // LystRepository.deleteList
            }
        }
    }

    private fun undeleteList() {
        deletedList
            ?.let { (index, list) ->
                _lists.value = _lists.value.toMutableList().also { it.add(index, list.apply { showHighlight = true }) }
                deletedList = null
                publishNewItemNotification(list)
                // LystRepository.restoreLyst
            }
    }

    private fun publishNewItemNotification(list: UIItem) {
        viewModelScope.launch {
            val index = lists.value.indexOf(list)
            if (index != -1) _newItem.emit(index)
        }
    }
}

object TestRepository {
    val lists: List<Lyst> = listOf(
        Lyst(
            name = "Groceries",
            items = listOf(
                Lyst.Item(description = "Milk", checked = false),
                Lyst.Item(description = "Eggs", checked = false),
                Lyst.Item(description = "Bread", checked = true),
                Lyst.Item(description = "Butter", checked = true),
                Lyst.Item(description = "Cheese", checked = true),
                Lyst.Item(description = "Apples", checked = false),
                Lyst.Item(description = "Oranges", checked = false),
                Lyst.Item(description = "Bananas", checked = false),
                Lyst.Item(description = "Blueberries", checked = true),
                Lyst.Item(description = "Raspberries", checked = true),
                Lyst.Item(description = "Grapes", checked = false),
                Lyst.Item(description = "Strawberries", checked = false),
                Lyst.Item(description = "Blackberries", checked = true),
                Lyst.Item(description = "Peaches", checked = true),
                Lyst.Item(description = "Plums", checked = true),
                Lyst.Item(description = "Pears", checked = true),
            )
        ),

        Lyst(
            name = "Backpacking",
            items = listOf(
                Lyst.Item(description = "Tent", checked = false),
                Lyst.Item(description = "Stove", checked = false),
                Lyst.Item(description = "Fuel", checked = true),
                Lyst.Item(description = "Backpack", checked = true),
                Lyst.Item(description = "Rain fly", checked = true),
                Lyst.Item(description = "Food", checked = false),
                Lyst.Item(description = "Water filter", checked = false),
                Lyst.Item(description = "Water bottle", checked = false),
                Lyst.Item(description = "Shoes", checked = true),
                Lyst.Item(description = "Hat", checked = true),
                Lyst.Item(description = "Sunglasses", checked = false),
                Lyst.Item(description = "First aid kit", checked = false),
                Lyst.Item(description = "Headlamp", checked = true),
                Lyst.Item(description = "Radio", checked = true),
                Lyst.Item(description = "Batteries", checked = true),
                Lyst.Item(description = "Sleeping bag", checked = true),
            )
        )
    )
}