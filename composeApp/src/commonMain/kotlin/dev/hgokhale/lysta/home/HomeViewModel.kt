package dev.hgokhale.lysta.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hgokhale.lysta.app.NavigationDestination
import dev.hgokhale.lysta.app.NavigationEvent
import dev.hgokhale.lysta.app.ScaffoldViewModel
import dev.hgokhale.lysta.app.SnackbarEvent
import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.repository.getRepository
import dev.hgokhale.lysta.utils.Highlightable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(val scaffoldViewModel: ScaffoldViewModel) : ViewModel() {
    data class UIItem(val id: String, val name: String, override var showHighlight: Boolean = false) : Highlightable

    private val _loaded = MutableStateFlow(false)
    val loaded = _loaded.asStateFlow()

    val repository = getRepository()
    private val _lists: MutableStateFlow<List<UIItem>> = MutableStateFlow(emptyList())
    val lists: StateFlow<List<UIItem>> = _lists.asStateFlow()

    private val _newItem = MutableSharedFlow<Int>() // index of a newly added item
    val newItem: SharedFlow<Int> get() = _newItem

    private var deletedList: Pair<Int, Lyst>? = null // first = index, second = list

    fun refreshListNames() {
        _lists.value = repository.getListNames().map { UIItem(id = it.id, name = it.name) }
        _loaded.value = true
    }

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
        repository.addList(lyst)

        publishNewItemNotification(newItem)
        viewModelScope.launch {
            scaffoldViewModel.navigationEvents.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(lyst.id)))
        }
        return lyst.id
    }

    fun onListClicked(id: String) {
        viewModelScope.launch { scaffoldViewModel.navigationEvents.send(NavigationEvent.Navigate(NavigationDestination.Lyst.routeFor(id))) }
    }

    fun deleteList(id: String) {
        viewModelScope.launch {
            val index = _lists.value.indexOfFirst { it.id == id }
            if (index != -1) {
                val listToDelete = _lists.value[index]
                _lists.value -= listToDelete
                repository.getList(listId = listToDelete.id)?.let { list ->
                    repository.deleteList(id)
                    deletedList = Pair(index, list)
                }
                scaffoldViewModel.snackbarEvents.send(
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
                _lists.value = _lists.value.toMutableList().also { it.add(index, UIItem(list.id, list.name, showHighlight = true)) }
                repository.restoreList(list, index)
                deletedList = null
                publishNewItemNotification(lists.value[index])
            }
    }

    private fun publishNewItemNotification(list: UIItem) {
        viewModelScope.launch {
            val index = lists.value.indexOf(list)
            if (index != -1) _newItem.emit(index)
        }
    }
}