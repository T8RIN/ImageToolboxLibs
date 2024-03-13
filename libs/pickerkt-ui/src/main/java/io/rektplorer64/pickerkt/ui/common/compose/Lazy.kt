package io.rektplorer64.pickerkt.ui.common.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

@OptIn(ExperimentalFoundationApi::class)
inline fun <T : Any> LazyGridScope.itemsIndexed(
    items: LazyPagingItems<T>,
    noinline span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    crossinline itemContent: @Composable (LazyItemScope.(index: Int, value: T?) -> Unit)
) {
    items(
        count = items.itemCount,
        span = if (span != null) {
            {
                span(items[it]!!)
            }
        } else {
            null
        }
    ) { index ->
        itemContent(index, items[index])
    }
}