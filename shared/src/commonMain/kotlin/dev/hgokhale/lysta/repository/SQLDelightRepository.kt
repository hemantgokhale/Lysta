package dev.hgokhale.lysta.repository

import dev.hgokhale.lysta.db.Database
import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.model.LystInfo

class SQLDelightRepository(val db: Database) : LystaRepository {

    override fun getListNames(): List<LystInfo> =
        db.listQueries.getListNames().executeAsList().map { LystInfo(it.name, it.id) }

    override fun getList(listId: String): Lyst? =
        db.listQueries.get(listId).executeAsOneOrNull()
            ?.let { dbList ->
                db.listItemQueries.selectAll(listId = listId).executeAsList()
                    .let { dbItems ->
                        val items = dbItems.map { dbItem -> Lyst.Item(description = dbItem.description, checked = dbItem.checked, id = dbItem.id) }
                        Lyst(name = dbList.name, isSorted = dbList.sorted, showChecked = dbList.showChecked, items = items, id = dbList.id)
                    }
            }

    override fun moveList(from: Int, to: Int) {
        if (from == to) return

        val allLists = db.listQueries.getListNames().executeAsList()
        if (from !in allLists.indices || to !in allLists.indices) return

        if (from < to)
            db.listQueries.moveDown(allLists[from].id, to.toLong())
        else
            db.listQueries.moveUp(allLists[from].id, to.toLong())
    }

    override fun addList(lyst: Lyst) {
        db.listQueries.newList(id = lyst.id, name = lyst.name, sorted = lyst.isSorted, showChecked = lyst.showChecked)
        lyst.items.forEach { addItem(lyst.id, it) }
    }

    override fun deleteList(listId: String) {
        db.listQueries.delete(listId)
    }

    override fun restoreList(list: Lyst, displayIndex: Int) {
        db.listQueries.insertAt(
            displayIndex = displayIndex.toLong(),
            id = list.id,
            name = list.name,
            sorted = list.isSorted,
            showChecked = list.showChecked,
        )
        list.items.forEachIndexed { index, item ->
            db.listItemQueries.insert(id = item.id, listId = list.id, description = item.description, checked = item.checked)
        }
    }

    override fun updateShowChecked(listId: String, showChecked: Boolean) {
        db.listQueries.updateShowChecked(showChecked = showChecked, id = listId)
    }

    override fun updateSorted(listId: String, sorted: Boolean) {
        db.listQueries.updateIsSorted(sorted = sorted, id = listId)
    }

    override fun updateName(listId: String, name: String) {
        db.listQueries.updateName(name = name, id = listId)
    }

    override fun addItem(listId: String, item: Lyst.Item) {
        db.listItemQueries.insert(id = item.id, description = item.description, checked = item.checked, listId = listId)
    }

    override fun deleteItem(listId: String, itemId: String) {
        db.listItemQueries.delete(itemId)
    }

    override fun restoreItem(listId: String, item: Lyst.Item, displayIndex: Int) {
        db.listItemQueries.insertAt(
            displayIndex = displayIndex.toLong(),
            id = item.id,
            listId = listId,
            description = item.description,
            checked = item.checked,
        )
    }

    override fun updateItemDescription(listId: String, itemId: String, description: String) {
        db.listItemQueries.updateDescription(description = description, id = itemId)
    }

    override fun updateItemChecked(listId: String, itemId: String, checked: Boolean) {
        db.listItemQueries.updateChecked(checked = checked, id = itemId)
    }

    override fun moveItem(listId: String, itemId: String, from: Int, to: Int) {
        if (from == to) return
        val allItems = db.listItemQueries.selectAll(listId = listId).executeAsList()
        if (from !in allItems.indices || to !in allItems.indices) return
        if (from < to)
            db.listItemQueries.moveDown(allItems[from].id, to.toLong())
        else
            db.listItemQueries.moveUp(allItems[from].id, to.toLong())
    }
}