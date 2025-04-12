package dev.hgokhale.lysta

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NavigationDestination(val route: String) {
    data object Home : NavigationDestination("home")
    data object Lyst : NavigationDestination("list/{listId}") {
        fun routeFor(listId: String) = "list/$listId"
    }
}

class LystViewModel : ViewModel() {
    sealed class UIState(val title: String, val showFAB: Boolean) {
        class Home : UIState(title = "My lists", showFAB = true)
        class Lyst(val lyst: dev.hgokhale.lysta.Lyst? = null, title: String = "") :
            UIState(title = title, showFAB = false) {
            constructor(lyst: dev.hgokhale.lysta.Lyst) : this(lyst = lyst, title = lyst.name.value)
        }
    }

    sealed interface UIEvent {
        data class Navigate(val route: String) : UIEvent
        data object NavigateBack : UIEvent
        data class Snackbar(val message: String, val actionLabel: String, val action: (() -> Unit)? = null) : UIEvent
    }

    private val _uiEvent = MutableSharedFlow<UIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _lists: SnapshotStateList<Lyst> = mutableStateListOf()
    val lists: List<Lyst> get() = _lists

    private val _uiState = MutableStateFlow<UIState>(UIState.Home())
    val uiState = _uiState.asStateFlow()

    private var deletedLists = mutableListOf<Lyst>()
    private var deletedItems = mutableListOf<Pair<String, Lyst.Item>>() // first = listId, second = item

    private fun createList(): String {
        val list = Lyst(name = "New list", listOf())
        _lists.add(list)
        return list.id
    }

    fun onListClicked(id: String) {
        viewModelScope.launch { _uiEvent.emit(UIEvent.Navigate(NavigationDestination.Lyst.routeFor(id))) }
    }

    fun onFabClicked() {
        viewModelScope.launch {
            when (uiState.value) {
                is UIState.Home -> _uiEvent.emit(UIEvent.Navigate(NavigationDestination.Lyst.routeFor(createList())))
                is UIState.Lyst -> {} // Currently not needed.
            }
        }
    }

    fun onBackArrowClicked() {
        viewModelScope.launch {
            _uiEvent.emit(UIEvent.NavigateBack)
        }
    }

    suspend fun loadList(id: String) {
        _lists
            .firstOrNull { it.id == id }
            ?.let {
                _uiState.value = UIState.Lyst()
                delay(500) // to simulate loading from server
                _uiState.value = UIState.Lyst(lyst = it)
            }
    }

    suspend fun goHome() {
        delay(500) // to simulate loading from server
        _uiState.value = UIState.Home()
    }

    fun deleteList(id: String) {
        viewModelScope.launch {
            _lists
                .firstOrNull { it.id == id }
                ?.let { lyst: Lyst ->
                    _lists.remove(lyst)
                    deletedLists.add(lyst)
                    _uiEvent.emit(
                        UIEvent.Snackbar(
                            message = "${lyst.name.value} deleted",
                            actionLabel = "Undo",
                            action = { undeleteList(id = id) }
                        )
                    )
                }
        }
    }

    private fun undeleteList(id: String) {
        deletedLists
            .firstOrNull { it.id == id }
            ?.let { lyst: Lyst ->
                deletedLists.remove(lyst)
                _lists.add(lyst)
            }
    }

    fun deleteItem(listId: String, itemId: String) {
        viewModelScope.launch {
            _lists
                .firstOrNull { it.id == listId }
                ?.let { lyst: Lyst ->
                    lyst.items
                        .firstOrNull { it.id == itemId }
                        ?.let { item: Lyst.Item ->
                            lyst.deleteItem(item)
                            deletedItems.add(Pair(listId, item))
                            _uiEvent.emit(
                                UIEvent.Snackbar(
                                    message = "${item.description.value} deleted",
                                    actionLabel = "Undo",
                                    action = { undeleteItem(listId = listId, itemId = item.id) }
                                )
                            )
                        }
                }
        }
    }

    private fun undeleteItem(listId: String, itemId: String) {
        deletedItems
            .firstOrNull { it.first == listId && it.second.id == itemId }
            ?.let { entry ->
                deletedItems.remove(entry)
                _lists
                    .firstOrNull { it.id == listId }
                    ?.let { lyst: Lyst ->
                        lyst.addItem(entry.second)
                    }
            }
    }

    init {
        _lists.add(
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
                )
            )
        )

        _lists.add(
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
                )
            )
        )
    }
}