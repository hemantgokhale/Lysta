package dev.hgokhale.lysta.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.NavigationDestination
import dev.hgokhale.lysta.app.NavigationEvent
import dev.hgokhale.lysta.app.NavigationEventBus
import dev.hgokhale.lysta.app.SnackbarEvent
import dev.hgokhale.lysta.app.SnackbarEventBus
import dev.hgokhale.lysta.lyst.Lyst
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LystaViewModel : ViewModel() {
    sealed class UIState(val title: String, val showFAB: Boolean) {
        class Home : UIState(title = "My lists", showFAB = true)
        class Lyst(val lyst: dev.hgokhale.lysta.lyst.Lyst? = null, title: String = "") : UIState(title = title, showFAB = false) {
            constructor(lyst: dev.hgokhale.lysta.lyst.Lyst) : this(lyst = lyst, title = lyst.name.value)

            val isListReady: Boolean get() = lyst != null
        }
    }

    private val _lists = MutableStateFlow(sampleLists())
    val lists: StateFlow<List<Lyst>> = _lists.asStateFlow()

    private val _uiState = MutableStateFlow<UIState>(UIState.Home())
    val uiState = _uiState.asStateFlow()

    private val _newItem = MutableSharedFlow<Int>() // index of a newly added item
    val newItem: SharedFlow<Int> get() = _newItem

    private var deletedList: Pair<Int, Lyst>? = null // first = index, second = list

    fun moveList(from: Int, to: Int) {
        if (from != to && from in _lists.value.indices && to in _lists.value.indices) {
            val mutableList = _lists.value.toMutableList()
            mutableList.add(to, mutableList.removeAt(from))
            _lists.value = mutableList
        }
    }

    private fun createList(): String {
        val list = Lyst(name = "New list", listOf(), viewModelScope)
        _lists.value += list
        publishNewItemNotification(list)
        return list.id
    }

    fun onListClicked(id: String) {
        viewModelScope.launch { NavigationEventBus.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(id))) }
    }

    fun onFabClicked() {
        viewModelScope.launch {
            when (uiState.value) {
                is UIState.Home -> NavigationEventBus.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(createList())))
                is UIState.Lyst -> {} // Currently not needed.
            }
        }
    }

    fun onBackArrowClicked() {
        viewModelScope.launch { NavigationEventBus.send(NavigationEvent.NavigateBack) }
    }

    fun loadList(id: String) {
        _lists.value
            .firstOrNull { it.id == id }
            ?.let { _uiState.value = UIState.Lyst(lyst = it) }
    }

    fun goHome() {
        _uiState.value = UIState.Home()
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
                        message = "Deleted: ${listToDelete.name.value}",
                        actionLabel = "Undo",
                        action = { undeleteList() }
                    )
                )
            }
        }
    }

    private fun undeleteList() {
        deletedList
            ?.let { (index, list) ->
                _lists.value = _lists.value.toMutableList().also { it.add(index, list.apply { showHighlight = true }) }
                deletedList = null
                publishNewItemNotification(list)
            }
    }

    fun deleteItem(listId: String, itemId: String) {
        viewModelScope.launch {
            _lists.value
                .firstOrNull { it.id == listId }
                ?.let { lyst: Lyst ->
                    lyst.deleteItem(itemId)
                        ?.let { item ->
                            SnackbarEventBus.send(
                                SnackbarEvent(
                                    message = "Deleted: ${item.description}",
                                    actionLabel = "Undo",
                                    action = { lyst.undeleteItem() }
                                )
                            )
                        }
                }
        }
    }

    fun onSortClicked() {
        _lists.value
            .firstOrNull { it.id == (uiState.value as? UIState.Lyst)?.lyst?.id }
            ?.let { lyst: Lyst ->
                lyst.onSortClicked()
            }
    }

    fun onShowCheckedClicked() {
        _lists.value
            .firstOrNull { it.id == (uiState.value as? UIState.Lyst)?.lyst?.id }
            ?.let { lyst: Lyst ->
                lyst.onShowCheckedClicked()
            }
    }

    private fun publishNewItemNotification(list: Lyst) {
        viewModelScope.launch {
            val index = lists.value.indexOf(list)
            if (index != -1) _newItem.emit(index)
        }
    }

    private fun sampleLists(): List<Lyst> = listOf(
        Lyst(
            "Groceries",
            listOf(
                Lyst.Item("Milk", false),
                Lyst.Item("Eggs", false),
                Lyst.Item("Bread", true),
                Lyst.Item("Butter", true),
                Lyst.Item("Cheese", true),
                Lyst.Item("Apples", false),
                Lyst.Item("Oranges", false),
                Lyst.Item("Bananas", false),
                Lyst.Item("Blueberries", true),
                Lyst.Item("Raspberries", true),
                Lyst.Item("Grapes", false),
                Lyst.Item("Strawberries", false),
                Lyst.Item("Blackberries", true),
                Lyst.Item("Peaches", true),
                Lyst.Item("Plums", true),
                Lyst.Item("Pears", true),
            ),
            viewModelScope = viewModelScope
        ),

        Lyst(
            "Backpacking",
            listOf(
                Lyst.Item("Tent", false),
                Lyst.Item("Stove", false),
                Lyst.Item("Fuel", true),
                Lyst.Item("Backpack", true),
                Lyst.Item("Rain fly", true),
                Lyst.Item("Food", false),
                Lyst.Item("Water filter", false),
                Lyst.Item("Water bottle", false),
                Lyst.Item("Shoes", true),
                Lyst.Item("Hat", true),
                Lyst.Item("Sunglasses", false),
                Lyst.Item("First aid kit", false),
                Lyst.Item("Headlamp", true),
                Lyst.Item("Radio", true),
                Lyst.Item("Batteries", true),
                Lyst.Item("Sleeping bag", true),
            ),
            viewModelScope = viewModelScope
        )
    )
}