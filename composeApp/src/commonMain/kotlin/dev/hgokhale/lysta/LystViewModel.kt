package dev.hgokhale.lysta

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Lyst : Screen("list/{listId}") {
        fun routeFor(listId: String) = "list/$listId"
    }
}

class Lyst(name: String, itemsValue: List<Item>) {
    @OptIn(ExperimentalUuidApi::class)
    val id = Uuid.random().toString()
    val items: SnapshotStateList<Item> = mutableStateListOf<Item>().also { it.addAll(itemsValue) }
    val name: MutableState<String> = mutableStateOf(name)
    val sorted: MutableState<Boolean> = mutableStateOf(false)

    @OptIn(ExperimentalUuidApi::class)
    class Item(descriptionValue: String, checkedValue: Boolean) {
        val checked: MutableState<Boolean> = mutableStateOf(checkedValue)
        val description: MutableState<String> = mutableStateOf(descriptionValue)
        val id = Uuid.random().toString()
    }

    fun addItem(description: String = "", checked: Boolean = false) {
        items.add(Item(description, checked))
    }

    fun deleteItem(item: Item) {
        items.remove(item)
    }

    override fun toString(): String = "Lyst: $name"
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
    }

    private val _uiEvent = MutableSharedFlow<UIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _lists: SnapshotStateList<Lyst> = mutableStateListOf()
    val lists: List<Lyst> get() = _lists

    private val _uiState = MutableStateFlow<UIState>(UIState.Home())
    val uiState = _uiState.asStateFlow()

    private fun createList(): String {
        val list = Lyst(name = "New list", listOf())
        _lists.add(list)
        return list.id
    }

    fun onListClicked(id: String) {
        viewModelScope.launch { _uiEvent.emit(UIEvent.Navigate(Screen.Lyst.routeFor(id))) }
    }

    fun onFabClicked() {
        viewModelScope.launch {
            when (uiState.value) {
                is UIState.Home -> _uiEvent.emit(UIEvent.Navigate(Screen.Lyst.routeFor(createList())))
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
        _lists.removeAll { it.id == id }
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