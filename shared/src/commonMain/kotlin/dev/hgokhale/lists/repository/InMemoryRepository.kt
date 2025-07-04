package dev.hgokhale.lists.repository

import dev.hgokhale.lists.model.Lyst
import dev.hgokhale.lists.model.LystInfo

object InMemoryRepository : LystaRepository {
    private val lists: MutableList<Lyst> = exampleLists

    override fun getListNames(): List<LystInfo> = lists.map { LystInfo(it.name, it.id) }

    override fun getList(listId: String): Lyst? = lists.find { it.id == listId }

    override fun moveList(from: Int, to: Int) {
        if (from != to && from in lists.indices && to in lists.indices) {
            lists.add(to, lists.removeAt(from))
        }
    }

    override fun addList(lyst: Lyst) {
        lists.add(lyst)
    }


    override fun deleteList(listId: String) {
        lists.removeAll { it.id == listId }
    }

    override fun restoreList(list: Lyst, displayIndex: Int) {
        lists.add(displayIndex, list)
    }

    override fun updateShowChecked(listId: String, showChecked: Boolean) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            lists[index] = lists[index].copy(showChecked = showChecked)
        }
    }

    override fun updateSorted(listId: String, sorted: Boolean) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            lists[index] = lists[index].copy(isSorted = sorted)
        }
    }

    override fun updateName(listId: String, name: String) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            lists[index] = lists[index].copy(name = name)
        }
    }

    override fun addItem(listId: String, item: Lyst.Item) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            lists[index] = lists[index].copy(items = lists[index].items + item)
        }
    }

    override fun deleteItem(listId: String, itemId: String) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { listIndex ->
            lists[listIndex].items.find { it.id == itemId }?.let { item ->
                lists[listIndex] = lists[listIndex].copy(items = lists[listIndex].items.filterNot { it.id == itemId })
            }
        }
    }

    override fun restoreItem(listId: String, item: Lyst.Item, displayIndex: Int) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            val newItems = lists[index].items.toMutableList().also { it.add(displayIndex, item) }
            lists[index] = lists[index].copy(items = newItems)
        }
    }

    private fun updateItem(listId: String, itemId: String, update: (Lyst.Item) -> Lyst.Item) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { listIndex ->
            lists[listIndex].items.indexOfFirstOrNull { it.id == itemId }?.let { itemIndex ->
                val updatedItem = update(lists[listIndex].items[itemIndex])
                val newItems = lists[listIndex].items.toMutableList().also { it[itemIndex] = updatedItem }
                lists[listIndex] = lists[listIndex].copy(items = newItems)
            }
        }
    }

    override fun updateItemDescription(listId: String, itemId: String, description: String) {
        updateItem(listId, itemId) { it.copy(description = description) }
    }

    override fun updateItemChecked(listId: String, itemId: String, checked: Boolean) {
        updateItem(listId, itemId) { it.copy(checked = checked) }
    }

    override fun moveItem(listId: String, itemId: String, from: Int, to: Int) {
        lists.indexOfFirstOrNull { it.id == listId }?.let { index ->
            lists[index].items.find { it.id == itemId }?.let { item ->
                val mutableItems = lists[index].items.toMutableList()
                mutableItems.add(to, mutableItems.removeAt(from))
                lists[index] = lists[index].copy(items = mutableItems)
            }
        }
    }
}

fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? = indexOfFirst(predicate).takeIf { it != -1 }