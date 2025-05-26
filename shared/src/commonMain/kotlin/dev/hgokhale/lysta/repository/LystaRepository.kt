package dev.hgokhale.lysta.repository

import dev.hgokhale.lysta.model.Lyst
import dev.hgokhale.lysta.model.LystInfo
import kotlinx.coroutines.flow.StateFlow

interface LystaRepository {
    val listNames: StateFlow<List<LystInfo>>
    fun getList(id: String): Lyst?
    fun moveList(from: Int, to: Int)
    fun newList(lyst: Lyst)
    fun deleteList(id: String)
    fun restoreList(index: Int, list: LystInfo)
    fun updateShowChecked(id: String, showChecked: Boolean)
    fun updateSorted(id: String, sorted: Boolean)
    fun updateName(id: String, name: String)
    fun addItem(id: String, item: Lyst.Item)
    fun deleteItem(listId: String, itemId: String)
    fun restoreItem(listId: String, itemId: String, itemIndex: Int)
    fun updateItemDescription(listId: String, itemId: String, description: String)
    fun updateItemChecked(listId: String, itemId: String, checked: Boolean)
    fun moveItem(listId: String, itemId: String, from: Int, to: Int)
}

expect fun getRepository(): LystaRepository