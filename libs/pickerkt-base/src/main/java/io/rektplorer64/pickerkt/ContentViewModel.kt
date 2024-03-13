package io.rektplorer64.pickerkt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import io.rektplorer64.pickerkt.builder.Ordering
import io.rektplorer64.pickerkt.builder.PickerKt
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.collection.datasource.CollectionFlow
import io.rektplorer64.pickerkt.content.datasource.ContentPagingSource
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.content.model.shouldSeparateApartFrom
import io.rektplorer64.pickerkt.contentresolver.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.threeten.bp.Instant
import timber.log.Timber

class ContentViewModel(application: Application, val collectionId: String, config: PickerKtConfiguration) : AndroidViewModel(application) {

    init {
        Timber.plant(tree = Timber.DebugTree())
    }

//    val collectionId = MutableStateFlow(initCollectionId)

//    val collectionListFlow = CollectionListingSource(
//        coroutineScope = viewModelScope,
//        context = application.applicationContext,
//        config = PickerKt.picker {
//            allowMimes {
//                add { MimeType.Jpeg }
//                add { MimeType.Png }
//                add { MimeType.Gif }
//                add { MimeType.Svg }
//                add { MimeType.Mpeg4 }
//                add { MimeType.MsWordDoc2007 }
//            }
//        }.also {
//            Timber.d("Config: $it")
//        }
//    ).flow.map { it.transform { it.filterNot { x -> x.id in PredefinedCollectionIdSet } } }.onEach {
//        Timber.d("collectionListFlow: ${it.data?.size}")
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionFlow = CollectionFlow(
        coroutineScope = viewModelScope,
        context = application.applicationContext,
        config = PickerKt.picker {
            allowMimes {
                addAll(config.mimeTypes)
            }
        },
        collectionId = collectionId.toLongOrNull()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val contentListFlow = Pager(config = PagingConfig(pageSize = 10, enablePlaceholders = true)) {
        ContentPagingSource(
            coroutineScope = viewModelScope,
            context = application.applicationContext,
            config = PickerKt.picker {
                allowMimes {
                    addAll(config.mimeTypes)
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
                    if (collectionId != AllFoldersCollection.id) {
                        ContentColumn(column = ContentResolverColumn.CollectionId) equal valueOf(collectionId)
                    }
                    ContentColumn(column = ContentResolverColumn.ByteSize) greaterThan valueOf(0)
                }
            }
        )
    }
        .flow
        .flowOn(Dispatchers.Default)
        .cachedIn(scope = viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val contentListTimeGroupedFlow = contentListFlow.mapLatest {
        it.insertSeparators { c1, c2 ->
            if (c1 == null && c2 != null) {
                return@insertSeparators LazyListItem.TimeGroupHeader(time = c2.dateAdded)
            }

            return@insertSeparators if (c1 != null && c2 != null) {
                if (c1 shouldSeparateApartFrom c2) LazyListItem.TimeGroupHeader(time = c2.dateAdded) else null
            } else {
                null
            }
        }
            .map { x ->
                when (x) {
                    is LazyListItem.TimeGroupHeader -> x
                    else -> LazyListItem.Data(x as Content)
                }
            }
    }
        .flowOn(Dispatchers.Default)
        .cachedIn(scope = viewModelScope)

    class Factory(
        private val application: Application,
        private val collectionId: String,
        private val config: PickerKtConfiguration = default
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ContentViewModel::class.java)) {
                ContentViewModel(
                    application = application,
                    collectionId = collectionId,
                    config = config
                ) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}

sealed class LazyListItem<T> {

    class Data<T>(val data: T) : LazyListItem<T>()

    class TimeGroupHeader(val time: Instant) : LazyListItem<Nothing>()

}