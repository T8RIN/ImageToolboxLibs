package io.rektplorer64.pickerkt.util

import android.database.Cursor

inline fun Cursor.forEach(block: Cursor.() -> Unit) {
    moveToFirst()
    do {
        block()
    } while (moveToNext())
}

inline fun Cursor.forEachIndexed(block: Cursor.(index: Int) -> Unit) {
    moveToFirst()
    var count = 0
    do {
        block(count)
        count++
    } while (moveToNext())
}

inline fun <T> Cursor.map(block: (Cursor) -> T): List<T> {
    val result = mutableListOf<T>()

    forEach {
        result.add(block(this))
    }
    return result
}

inline fun <T> Cursor.pagedMap(offset: Int, limit: Int, block: (Cursor) -> T): List<T> {
    var i = 0
    val result = mutableListOf<T>()
    while (moveToPosition(offset + i)) {
        result.add(block(this))

        i++
        if (i >= limit) break
    }
    return result
}