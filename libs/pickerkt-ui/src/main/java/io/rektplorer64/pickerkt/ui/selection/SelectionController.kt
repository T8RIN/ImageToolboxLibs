package io.rektplorer64.pickerkt.ui.selection

import io.rektplorer64.pickerkt.common.property.Identifiable
import io.rektplorer64.pickerkt.content.model.Content

interface SelectionController<T : Identifiable<ID>, ID> {

    fun select(item: T): Boolean

    fun select(item: List<T>)

    val size: Int

    fun unselect(item: T): Boolean

    fun toggleSelection(item: T)

    fun clear()

    fun removeIf(predicate: (T) -> Boolean)

    fun invert(items: Collection<T>)

    operator fun contains(itemId: ID): Boolean

    operator fun contains(item: T): Boolean

    fun replaceAllWith(newSelection: List<T>)

    val canonicalSelectionList: List<Content>

    val canonicalSelectionOrderMap: Map<Long, Int>
}

fun SelectionController<*, *>.isEmpty(): Boolean {
    return size == 0
}

fun SelectionController<*, *>.isNotEmpty(): Boolean {
    return size > 0
}

