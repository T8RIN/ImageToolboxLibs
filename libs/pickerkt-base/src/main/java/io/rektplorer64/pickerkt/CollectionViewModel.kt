package io.rektplorer64.pickerkt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.rektplorer64.pickerkt.builder.PickerKt
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.collection.datasource.CollectionListingSource
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.common.data.transform
import io.rektplorer64.pickerkt.contentresolver.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

val default = PickerKt.picker {
    allowMimes {
        add { MimeType.Jpeg }
        add { MimeType.Png }
        add { MimeType.Gif }
        add { MimeType.Svg }
        add { MimeType.Mpeg4 }
        add { MimeType.MsWordDoc2007 }
        add { MimeType.Mp3 }
        add { MimeType.OggAudio }
    }
}

class CollectionViewModel(application: Application, config: PickerKtConfiguration) :
    AndroidViewModel(application) {

    val collectionListFlow = CollectionListingSource(
        coroutineScope = viewModelScope,
        context = application.applicationContext,
        config = config
    ).flow
        .map {
            it.transform { data ->
                data.filterNot { x ->
                    x.id in PredefinedCollectionIdSet
                }
            }
        }
        .onEach {
            Timber.d("collectionListFlow: ${it.data?.size}")
        }

    class Factory(
        private val application: Application,
        private val config: PickerKtConfiguration = default
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
                CollectionViewModel(application = application, config = config) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}