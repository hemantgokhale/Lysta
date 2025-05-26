package dev.hgokhale.lysta.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.NavigationDestination
import dev.hgokhale.lysta.app.NavigationEvent
import dev.hgokhale.lysta.app.NavigationEventBus
import dev.hgokhale.lysta.app.SnackbarEvent
import dev.hgokhale.lysta.app.SnackbarEventBus
import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.model.LystInfo
import dev.hgokhale.lysta.repository.InMemoryRepository
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

    val repository = InMemoryRepository
    private val _lists: MutableStateFlow<List<UIItem>> = MutableStateFlow(emptyList())
    val lists: StateFlow<List<UIItem>> = _lists.asStateFlow()

    init {
        viewModelScope.launch {
            repository.listNames.collect { names ->
                _lists.value = names.map { UIItem(id = it.id, name = it.name) }
                _loaded.value = true
            }
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
            repository.moveList(from, to)
        }
    }

    fun createList(): String {
        val lyst = Lyst(name = "New list")
        val newItem = UIItem(lyst.id, lyst.name)
        _lists.value += newItem
        repository.newList(lyst)

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
                repository.deleteList(id)

                deletedList = Pair(index, listToDelete)
                SnackbarEventBus.send(
                    SnackbarEvent(
                        message = "Deleted: ${listToDelete.name}",
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
                repository.restoreList(index, LystInfo(list.id, list.name))
                deletedList = null
                publishNewItemNotification(list)
            }
    }

    private fun publishNewItemNotification(list: UIItem) {
        viewModelScope.launch {
            val index = lists.value.indexOf(list)
            if (index != -1) _newItem.emit(index)
        }
    }
}