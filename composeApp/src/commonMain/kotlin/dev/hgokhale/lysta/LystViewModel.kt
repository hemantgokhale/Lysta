package dev.hgokhale.lysta

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    val lists: SnapshotStateList<Lyst> = mutableStateListOf()
    private val _uiState = MutableStateFlow<UIState>(UIState.Home())
    val uiState = _uiState.asStateFlow()

    fun newList(): String {
        val list = Lyst(name = "New list", listOf())
        lists.add(list)
        _uiState.value = UIState.Lyst(lyst = list)
        return list.id
    }

    suspend fun loadList(id: String) {
        lists
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

    init {
        lists.add(
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

        lists.add(
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