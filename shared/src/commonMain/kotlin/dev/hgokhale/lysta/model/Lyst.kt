package dev.hgokhale.lysta.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val NEW_LIST_NAME = "New List"

@OptIn(ExperimentalUuidApi::class)
data class Lyst(
    val name: String = NEW_LIST_NAME,
    val isSorted: Boolean = false,
    val showChecked: Boolean = true,
    val items: List<Item> = emptyList(),
    val id: String = Uuid.random().toString(),
) {
    data class Item(
        val description: String,
        val checked: Boolean = false,
        val id: String = Uuid.random().toString(),
    )
    val isNew: Boolean = name == NEW_LIST_NAME && items.isEmpty()
}

