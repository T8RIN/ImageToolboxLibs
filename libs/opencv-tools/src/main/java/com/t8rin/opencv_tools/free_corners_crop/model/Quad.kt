package com.t8rin.opencv_tools.free_corners_crop.model

import android.graphics.PointF
import org.opencv.core.Point
import kotlin.math.pow
import kotlin.math.sqrt

class Quad(
    val topLeftCorner: PointF,
    val topRightCorner: PointF,
    val bottomRightCorner: PointF,
    val bottomLeftCorner: PointF
)

fun PointF.toOpenCVPoint(): Point {
    return Point(x.toDouble(), y.toDouble())
}

fun Point.distance(point: Point): Double {
    return sqrt((point.x - x).pow(2) + (point.y - y).pow(2))
}