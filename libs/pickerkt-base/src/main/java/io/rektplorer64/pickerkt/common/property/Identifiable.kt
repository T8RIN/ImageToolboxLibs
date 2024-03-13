package io.rektplorer64.pickerkt.common.property

interface Identifiable<T> {
    val id: T
    val name: String
}

fun <ID, T : Identifiable<ID>> List<T>.findOneById(id: ID): T? {
    return firstOrNull { it.id == id }
}

fun <ID, T : Identifiable<ID>> List<T>.groupById(): Map<ID, List<T>> {
    return groupBy { it.id }
}