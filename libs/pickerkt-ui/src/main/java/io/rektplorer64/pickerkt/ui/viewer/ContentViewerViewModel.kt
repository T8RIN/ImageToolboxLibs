package io.rektplorer64.pickerkt.ui.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.rektplorer64.pickerkt.content.datasource.ContentSingleSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ContentViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val _previewContentId = MutableStateFlow<Long?>(null)
    val previewContentId = _previewContentId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val previewContent = _previewContentId.flatMapLatest {
        if (it != null) {
            ContentSingleSource(
                coroutineScope = viewModelScope,
                context = application,
                contentId = it,
            ).flow
        } else {
            flowOf(null)
        }
    }

    fun setPreviewContentId(contentId: Long?) {
        _previewContentId.tryEmit(contentId)
    }
}