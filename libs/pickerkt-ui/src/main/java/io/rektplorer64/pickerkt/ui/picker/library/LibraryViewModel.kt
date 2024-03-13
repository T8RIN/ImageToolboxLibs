package io.rektplorer64.pickerkt.ui.picker.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.rektplorer64.pickerkt.builder.Ordering
import io.rektplorer64.pickerkt.builder.PickerKt
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.collection.datasource.CollectionListingSource
import io.rektplorer64.pickerkt.content.datasource.ContentPagingSource
import io.rektplorer64.pickerkt.contentresolver.ContentResolverColumn
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.contentresolver.Order
import io.rektplorer64.pickerkt.default
import kotlinx.coroutines.ExperimentalCoroutinesApi

class LibraryViewModel(application: Application, mimeTypeGroup: MimeType.Group?, config: PickerKtConfiguration) : AndroidViewModel(application) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionList = CollectionListingSource(
        coroutineScope = viewModelScope,
        context = application.applicationContext,
        config = PickerKt.picker {
            allowMimes {
                addAll(config.mimeTypes.filter { it.group == mimeTypeGroup })
            }
        }
    ).flow

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentContentList = Pager(config = PagingConfig(pageSize = 20, enablePlaceholders = true)) {
        ContentPagingSource(
            coroutineScope = viewModelScope,
            context = application.applicationContext,
            config = PickerKt.picker {
                allowMimes {
                    addAll(config.mimeTypes.filter { it.group == mimeTypeGroup })
                }
                orderBy {
                    add {
                        Ordering(
                            column = ContentResolverColumn.DateAdded,
                            order = Order.Descending
                        )
                    }
                }
                predicate {
                    ContentColumn(column = ContentResolverColumn.ByteSize) greaterThan valueOf(0)
                }
            }
        )
    }.flow.cachedIn(viewModelScope)

    class Factory(
        private val application: Application,
        private val mimeTypeGroup: MimeType.Group?,
        private val config: PickerKtConfiguration = default
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                LibraryViewModel(
                    application = application,
                    mimeTypeGroup = mimeTypeGroup,
                    config = config
                ) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}