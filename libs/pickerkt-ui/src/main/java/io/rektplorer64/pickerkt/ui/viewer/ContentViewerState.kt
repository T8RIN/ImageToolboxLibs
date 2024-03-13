package io.rektplorer64.pickerkt.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.rektplorer64.pickerkt.ui.layout.SidePaneBottomSheetLayoutState

class ContentPreviewState(
    initialInfoSectionVisibility: Boolean = false,
    initialControlVisibility: Boolean = true
) {
    private val _controlVisible = mutableStateOf(initialControlVisibility)
    val controlVisible: State<Boolean> = _controlVisible

    internal val infoState = SidePaneBottomSheetLayoutState(initialInfoSectionVisibility)
    val infoSectionVisible = infoState.state

    fun openInfoSection() {
        infoState.open()
    }

    fun closeInfoSection() {
        infoState.close()
    }

    fun toggleInfoSection() {
        infoState.toggle()
    }

    fun showControl() {
        _controlVisible.value = true
    }

    fun hideControl() {
        _controlVisible.value = false
    }

    fun toggleControlVisibility() {
        _controlVisible.value = !_controlVisible.value
    }
}

@Composable
fun rememberContentPreviewState(
    initialInfoSectionVisibility: Boolean = false,
    initialControlVisibility: Boolean = true
) = remember {
    ContentPreviewState(
        initialInfoSectionVisibility = initialInfoSectionVisibility,
        initialControlVisibility = initialControlVisibility
    )
}