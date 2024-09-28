package com.t8rin.curves

import android.content.Context
import android.graphics.PointF
import coil.transform.Transformation
import com.t8rin.curves.view.GPUFilterTransformation
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesToolValue
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter

data class ImageCurvesEditorState internal constructor(
    internal val curvesToolValue: CurvesToolValue
) {
    internal fun buildFilter(): GPUImageFilter = GPUImageToneCurveFilter().apply {
        setRgbCompositeControlPoints(
            curvesToolValue.luminanceCurve.toPoints()
        )
        setRedControlPoints(
            curvesToolValue.redCurve.toPoints()
        )
        setGreenControlPoints(
            curvesToolValue.greenCurve.toPoints()
        )
        setBlueControlPoints(
            curvesToolValue.blueCurve.toPoints()
        )
        updateToneCurveTexture()
    }

    fun asCoilTransformation(
        context: Context
    ): Transformation = object : GPUFilterTransformation(context) {
        override fun createFilter(): GPUImageFilter = buildFilter()

        override val cacheKey: String
            get() = curvesToolValue.hashCode().toString()
    }

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