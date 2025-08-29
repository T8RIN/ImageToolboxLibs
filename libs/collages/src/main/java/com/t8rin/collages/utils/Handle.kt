package com.t8rin.collages.utils

import android.graphics.PointF

internal abstract class Handle {
    open fun getAngle(): Float? {return 0f}
    abstract fun draggablePoint(manager: ParamsManager): PointF
    abstract fun tryDrag(point: PointF, manager: ParamsManager): PointF?
}

internal abstract class LinearHandle(
    private val managedParam: ParamT,
    private val direction: PointF
) : Handle() {
    override fun getAngle(): Float? {
        return Math.toDegrees(kotlin.math.atan2(direction.y, direction.x).toDouble()).toFloat()
    }

    protected abstract fun computeDraggablePoint(values: FloatArray): PointF
    protected abstract fun pointToValue(point: PointF): Float

    override fun draggablePoint(manager: ParamsManager): PointF =
        computeDraggablePoint(manager.valuesRef())

    override fun tryDrag(point: PointF, manager: ParamsManager): PointF? {
        val values = manager.valuesRef()
        val initialPoint = computeDraggablePoint(values)

        val dx = point.x - initialPoint.x
        val dy = point.y - initialPoint.y

        val norm = direction.x * dx + direction.y * dy
        val clippedPoint = PointF(
            initialPoint.x + direction.x * norm,
            initialPoint.y + direction.y * norm
        )

        val newValue = pointToValue(clippedPoint)

        return try {
            manager.updateParams(listOf(managedParam), floatArrayOf(newValue))
            clippedPoint
        } catch (e: ParamsManager.InvalidValues) {
            null
        }
    }
}

internal class XHandle(
    private val managedParam: ParamT,
    private val yProvider: (values: FloatArray) -> Float
) : LinearHandle(managedParam, PointF(1f, 0f)) {
    override fun computeDraggablePoint(values: FloatArray): PointF =
        PointF(values[managedParam], yProvider(values))

    override fun pointToValue(point: PointF): Float = point.x
}

internal class YHandle(
    private val xProvider: (values: FloatArray) -> Float,
    private val managedParam: ParamT
) : LinearHandle(managedParam, PointF(0f, 1f)) {
    override fun computeDraggablePoint(values: FloatArray): PointF =
        PointF(xProvider(values), values[managedParam])

    override fun pointToValue(point: PointF): Float = point.y
}
