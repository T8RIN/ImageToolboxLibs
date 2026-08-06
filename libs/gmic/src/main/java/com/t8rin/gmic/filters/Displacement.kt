package com.t8rin.gmic.filters

import android.graphics.Bitmap
import com.t8rin.gmic.GmicFilter

enum class DisplacementInterpolation(val value: Int) {
    Nearest(0),
    Linear(1),
    Cubic(2)
}

enum class DisplacementBoundary(val value: Int) {
    Transparent(0),
    Clamp(1),
    Wrap(2),
    Mirror(3)
}

data class Displacement(
    val horizontalMap: Bitmap,
    val verticalMap: Bitmap? = null,
    val strengthX: Float = 0.1f,
    val strengthY: Float = 0.1f,
    val interpolation: DisplacementInterpolation = DisplacementInterpolation.Linear,
    val boundary: DisplacementBoundary = DisplacementBoundary.Clamp
) : GmicFilter {

    init {
        require(strengthX in -1f..1f) { "strengthX must be in -1..1" }
        require(strengthY in -1f..1f) { "strengthY must be in -1..1" }
    }

    override val auxiliaryInputs: List<Bitmap> = listOf(
        horizontalMap,
        verticalMap ?: horizontalMap
    )

    override val command: String = buildString {
        append("channels[1,2] 0,2 luminance[1,2] ")
        append("resize[1,2] {w#0},{h#0},1,1,3 ")
        append("sub[1] 127.5 div[1] 127.5 mul[1] {w#0*")
        append(strengthX)
        append("} ")
        append("sub[2] 127.5 div[2] 127.5 mul[2] {h#0*")
        append(strengthY)
        append("} ")
        append("append[1,2] c warp[0] [1],1,")
        append(interpolation.value)
        append(',')
        append(boundary.value)
        append(" keep[0]")
    }

    override val options = gmicProcessAlpha
}
