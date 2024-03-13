package io.rektplorer64.pickerkt.ui.selection

import androidx.lifecycle.ViewModel
import io.rektplorer64.pickerkt.content.model.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ContentSelectionViewModel : ViewModel() {
    private val _selection = MutableStateFlow<List<Content>>(listOf())
    val selection = _selection.asStateFlow()

    fun replaceSelection(newList: List<Content>) {
        _selection.tryEmit(newList)
    }
}