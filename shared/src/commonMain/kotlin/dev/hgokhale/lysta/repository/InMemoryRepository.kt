package dev.hgokhale.lysta.repository

import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.model.LystInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryRepository: LystaRepository {
    private val lists: MutableList<Lyst> = exampleLists
    private var deletedList: Lyst? = null
    private var deletedItem: Lyst.Item? = null

    private val _listNames = MutableStateFlow<List<LystInfo>>(lists.map { LystInfo(it.name, it.id) })
    override val listNames: StateFlow<List<LystInfo>> = _listNames.asStateFlow()

    override fun getList(id: String): Lyst? = lists.find { it.id == id }

    override fun moveList(from: Int, to: Int) {
        if (from != to && from in lists.indices && to in lists.indices) {
            lists.add(to, lists.removeAt(from))
            updateListNames()
        }
    }

    private fun updateListNames() {
        _listNames.value = lists.map { LystInfo(it.name, it.id) }
    }

    override fun newList(lyst: Lyst) {
        lists.add(lyst)
    }

    override fun deleteList(id: String) {
        lists.find { it.id == id }?.let {
            lists.remove(it)
            deletedList = it
            updateListNames()
        }
    }

    override fun restoreList(index: Int, list: LystInfo) {
        deletedList?.let {
            if (it.id == list.id) lists.add(index, it)
            deletedList = null
            updateListNames()
        }
    }

    override fun updateShowChecked(id: String, showChecked: Boolean) {
        indexOrNull(id)?.let { index ->
            lists[index] = lists[index].copy(showChecked = showChecked)
        }
    }

    override fun updateSorted(id: String, sorted: Boolean) {
        indexOrNull(id)?.let { index ->
            lists[index] = lists[index].copy(isSorted = sorted)
        }
    }

    override fun updateName(id: String, name: String) {
        indexOrNull(id)?.let { index ->
            lists[index] = lists[index].copy(name = name)
            updateListNames()
        }
    }

    override fun addItem(id: String, item: Lyst.Item) {
        indexOrNull(id)?.let { index ->
            lists[index] = lists[index].copy(items = lists[index].items + item)
        }
    }

    override fun deleteItem(listId: String, itemId: String) {
        indexOrNull(listId)?.let { index ->
            lists[index].items.find { it.id == itemId }?.let { item ->
                lists[index] = lists[index].copy(items = lists[index].items - item)
                deletedItem = item
            }
        }
    }

    override fun restoreItem(listId: String, itemId: String, itemIndex: Int) {
        indexOrNull(listId)?.let { index ->
            deletedItem?.let { item ->
                if (item.id == itemId) {
                    val modifiedItems = lists[index].items.toMutableList()
                    modifiedItems.add(itemIndex, item)
                    lists[index] = lists[index].copy(items = modifiedItems)
                    deletedItem = null
                }
            }
        }
    }

    override fun updateItemDescription(listId: String, itemId: String, description: String) {
        indexOrNull(listId)?.let { index ->
            lists[index].items.find { it.id == itemId }?.let { item ->
                lists[index] = lists[index].copy(items = lists[index].items.map {
                    if (it.id == itemId) it.copy(description = description) else it
                })
            }
        }
    }

    override fun updateItemChecked(listId: String, itemId: String, checked: Boolean) {
        indexOrNull(listId)?.let { index ->
            lists[index].items.find { it.id == itemId }?.let { item ->
                lists[index] = lists[index].copy(items = lists[index].items.map {
                    if (it.id == itemId) it.copy(checked = checked) else it
                })
            }
        }
    }

    override fun moveItem(listId: String, itemId: String, from: Int, to: Int) {
        indexOrNull(listId)?.let { index ->
            lists[index].items.find { it.id == itemId }?.let { item ->
                val mutableItems = lists[index].items.toMutableList()
                mutableItems.add(to, mutableItems.removeAt(from))
                lists[index] = lists[index].copy(items = mutableItems)
            }
        }
    }

    private fun indexOrNull(id: String): Int? {
        val index = lists.indexOfFirst { it.id == id }
        return if (index != -1) index else null
    }
}

private val exampleLists: MutableList<Lyst> = mutableListOf(
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