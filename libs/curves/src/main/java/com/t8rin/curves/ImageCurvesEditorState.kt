package com.t8rin.curves

import android.graphics.PointF
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesToolValue
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter

@ConsistentCopyVisibility
data class ImageCurvesEditorState internal constructor(
    internal val curvesToolValue: CurvesToolValue
) {
    internal fun buildFilter(): GPUImageFilter = GPUImageToneCurveFilter().apply {
        setAllControlPoints(getControlPoints())
    }

    fun isDefault(): Boolean = listOf(
        curvesToolValue.luminanceCurve,
        curvesToolValue.redCurve,
        curvesToolValue.greenCurve,
        curvesToolValue.blueCurve
    ).all { it.isDefault }

    fun exportControlPoints(): List<List<Float>> =
        getControlPoints().map { list -> list.map { it.y } }

    fun initControlPoints(controlPoints: List<List<Float>>) {
        curvesToolValue.luminanceCurve.setPoints(controlPoints[0])
        curvesToolValue.redCurve.setPoints(controlPoints[1])
        curvesToolValue.greenCurve.setPoints(controlPoints[2])
        curvesToolValue.blueCurve.setPoints(controlPoints[3])
    }

    private fun getControlPoints(): List<Array<PointF>> = listOf(
        curvesToolValue.luminanceCurve.toPoints(),
        curvesToolValue.redCurve.toPoints(),
        curvesToolValue.greenCurve.toPoints(),
        curvesToolValue.blueCurve.toPoints()
    )

    private fun CurvesValue.setPoints(points: List<Float>) {
        blacksLevel = points[0] * 100f
        shadowsLevel = points[1] * 100f
        midtonesLevel = points[2] * 100f
        highlightsLevel = points[3] * 100f
        whitesLevel = points[4] * 100f
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