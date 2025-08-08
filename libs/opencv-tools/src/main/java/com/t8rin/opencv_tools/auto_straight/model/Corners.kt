package com.t8rin.opencv_tools.auto_straight.model

data class Corners(
    val topLeft: PointD,
    val topRight: PointD,
    val bottomRight: PointD,
    val bottomLeft: PointD,
    val isAbsolute: Boolean = true
) {
    val points = listOf(topLeft, topRight, bottomRight, bottomLeft)
}