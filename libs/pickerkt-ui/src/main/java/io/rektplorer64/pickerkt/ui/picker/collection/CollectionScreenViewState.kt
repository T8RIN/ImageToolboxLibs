package io.rektplorer64.pickerkt.ui.picker.collection

import android.content.res.Resources
import androidx.compose.runtime.*
import androidx.paging.LoadState

enum class CollectionScreenViewState {
    Content,
    Empty,
    RefreshError,
    Loading;

    companion object {
        fun determineState(
            itemCount: Int,
            pagingRefreshState: LoadState
        ): CollectionScreenViewState = when(pagingRefreshState) {
            LoadState.Loading -> Loading
            is LoadState.NotLoading -> if (itemCount == 0) Loading else Content
            is LoadState.Error -> if (pagingRefreshState.error is Resources.NotFoundException) {
                Empty
            } else {
                RefreshError
            }
        }

        @Composable
        fun rememberCollectionScreenViewState(
            itemCount: Int,
            pagingRefreshState: LoadState
        ): CollectionScreenViewState {
            var viewState by remember { mutableStateOf(Content) }
            LaunchedEffect(itemCount, pagingRefreshState) {
                viewState = determineState(itemCount = itemCount, pagingRefreshState = pagingRefreshState)
            }

            return viewState
        }
    }
}