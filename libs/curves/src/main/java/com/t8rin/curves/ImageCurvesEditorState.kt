@file:Suppress("unused")

package com.t8rin.curves

import android.graphics.PointF
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesToolValue
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter

@ConsistentCopyVisibility
data class ImageCurvesEditorState internal constructor(
    internal val curvesToolValue: CurvesToolValue
) {
    constructor(controlPoints: List<List<Float>>) : this(CurvesToolValue()) {
        initControlPoints(controlPoints)
    }

    fun copy(
        controlPoints: List<List<Float>>
    ): ImageCurvesEditorState = ImageCurvesEditorState(controlPoints)

    internal fun buildFilter(): GPUImageToneCurveFilter = GPUImageToneCurveFilter().apply {
        setAllControlPoints(getControlPointsImpl())
    }

    fun isDefault(): Boolean = listOf(
        curvesToolValue.luminanceCurve,
        curvesToolValue.redCurve,
        curvesToolValue.greenCurve,
        curvesToolValue.blueCurve
    ).all { it.isDefault }

    val controlPoints: List<List<Float>>
        get() = getControlPointsImpl().map { list -> list.map { it.y } }

    private fun initControlPoints(controlPoints: List<List<Float>>) {
        curvesToolValue.luminanceCurve.setPoints(controlPoints[0])
        curvesToolValue.redCurve.setPoints(controlPoints[1])
        curvesToolValue.greenCurve.setPoints(controlPoints[2])
        curvesToolValue.blueCurve.setPoints(controlPoints[3])
    }

    private fun getControlPointsImpl(): List<Array<PointF>> = listOf(
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

fun GPUImageToneCurveFilter(
    controlPoints: List<List<Float>>
): GPUImageToneCurveFilter = GPUImageToneCurveFilter(
    state = ImageCurvesEditorState(controlPoints)
)

fun GPUImageToneCurveFilter(
    state: ImageCurvesEditorState
): GPUImageToneCurveFilter = state.buildFilter()