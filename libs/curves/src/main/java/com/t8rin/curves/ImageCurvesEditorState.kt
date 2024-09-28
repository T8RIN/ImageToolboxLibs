package com.t8rin.curves

import com.t8rin.curves.PhotoFilterCurvesControl.CurvesToolValue

data class ImageCurvesEditorState internal constructor(
    internal val curvesToolValue: CurvesToolValue
) {
    companion object {
        val Default: ImageCurvesEditorState
            get() = ImageCurvesEditorState(CurvesToolValue())
    }
}