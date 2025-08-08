package com.t8rin.opencv_tools.auto_straight.model

import androidx.annotation.IntRange

sealed class StraightenMode {
    data object Perspective : StraightenMode()

    data class Deskew(
        @param:IntRange(0, 90) val maxSkew: Int = 10,
        val allowCrop: Boolean = true
    ) : StraightenMode()

    data class Manual(val corners: Corners) : StraightenMode()
}