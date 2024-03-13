package io.rektplorer64.pickerkt.ui.viewer

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.first(predicate: (T) -> Boolean): T? {
    for (i in 0 until itemCount) {
        val data = this[i]!!
        if (predicate(data)) {
            return data
        }
    }
    return null
}

fun <T : Any> LazyPagingItems<T>.indexOfFirst(predicate: (T) -> Boolean): Int {
    var ongoingItemCount = itemCount
    var i = 0
    while (i < ongoingItemCount) {
        val data = this[i]!!
        if (predicate(data)) {
            return i
        }
        i++
        ongoingItemCount = itemCount
    }

    return -1
}

/**
 * **Reference:** [Medium.com](https://andreclassen1337.medium.com/create-android-compose-lazylist-scroll-effects-af5a423a53e6)
 */
fun LazyListLayoutInfo.normalizedItemPosition(key: Any): Float =
    visibleItemsInfo
        .firstOrNull { it.key == key }
        ?.let {
            val center = (viewportEndOffset + viewportStartOffset - it.size) / 2F
            (it.offset.toFloat() - center) / center
        }
        ?: 0F