package dev.hgokhale.lysta.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Lyst(
    val name: String,
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
}

