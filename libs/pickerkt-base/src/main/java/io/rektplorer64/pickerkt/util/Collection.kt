package io.rektplorer64.pickerkt.util

inline fun <T> Array<out T>.foldFromFirst(operation: (acc: T, T) -> T): T {
    return slice(1 until size).fold(initial = first(), operation = { acc, t -> operation(acc, t) })
}

inline fun <reified T> List<T>.swapFirstAndLast(): List<T> {

    if (isEmpty() || size == 1) return this

    val mutableList = toMutableList().also {
        it.removeLastOrNull()
        it.removeFirstOrNull()
    }

    return listOf(last(), *mutableList.toTypedArray(), first())
}