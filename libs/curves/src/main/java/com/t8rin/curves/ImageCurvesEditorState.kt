package com.t8rin.curves

import android.graphics.PointF
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesToolValue
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter

data class ImageCurvesEditorState internal constructor(
    internal val curvesToolValue: CurvesToolValue
) {
    internal fun buildFilter(): GPUImageFilter = GPUImageToneCurveFilter().apply {
        setAllControlPoints(getControlPoints())
    }

    fun getControlPoints(): List<Array<PointF>> = listOf(
        curvesToolValue.luminanceCurve.toPoints(),
        curvesToolValue.redCurve.toPoints(),
        curvesToolValue.greenCurve.toPoints(),
        curvesToolValue.blueCurve.toPoints()
    )

    private fun CurvesValue.toPoints(): Array<PointF> = listOf(
        PointF(0.0f, blacksLevel / 100f),
        PointF(0.25f, shadowsLevel / 100f),
        PointF(0.5f, midtonesLevel / 100f),
        PointF(0.75f, highlightsLevel / 100f),
        PointF(1.0f, whitesLevel / 100f),
    ).toTypedArray()

    companion object {
        val Default: ImageCurvesEditorState
            get() = ImageCurvesEditorState(CurvesToolValue())
    }
}