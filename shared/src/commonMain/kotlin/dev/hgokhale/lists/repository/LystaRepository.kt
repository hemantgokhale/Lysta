package dev.hgokhale.lists.repository

import dev.hgokhale.lists.db.Database
import dev.hgokhale.lists.db.createSQLDelightDriver
import dev.hgokhale.lists.model.Lyst
import dev.hgokhale.lists.model.LystInfo

interface LystaRepository {
    fun getListNames(): List<LystInfo>
    fun getList(listId: String): Lyst?
    fun moveList(from: Int, to: Int)
    fun addList(lyst: Lyst)
    fun deleteList(listId: String)
    fun restoreList(list: Lyst, displayIndex: Int)
    fun updateShowChecked(listId: String, showChecked: Boolean)
    fun updateSorted(listId: String, sorted: Boolean)
    fun updateName(listId: String, name: String)
    fun addItem(listId: String, item: Lyst.Item)
    fun deleteItem(listId: String, itemId: String)
    fun restoreItem(listId: String, item: Lyst.Item, displayIndex: Int)
    fun updateItemDescription(listId: String, itemId: String, description: String)
    fun updateItemChecked(listId: String, itemId: String, checked: Boolean)
    fun moveItem(listId: String, itemId: String, from: Int, to: Int)
}

fun getRepository(): LystaRepository =
    createSQLDelightDriver()
        ?.let {
            val database = Database(it)
            SQLDelightRepository(database)
        }
        ?: run { InMemoryRepository }

val exampleLists: MutableList<Lyst> = mutableListOf(
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