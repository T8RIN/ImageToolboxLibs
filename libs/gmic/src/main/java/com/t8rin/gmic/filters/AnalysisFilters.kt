package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

enum class IsophoteFilling(override val value: Int) : GmicArgument {
    Transparent(0),
    Colors(1)
}

data class Isophotes(
    val levels: Int = 8,
    val smoothness: Float = 4f,
    val filling: IsophoteFilling = IsophoteFilling.Colors
) : RawGmicFilter(
    gmicCommand(
        "fx_isophotes",
        levels.inRange("levels", 1..256),
        smoothness.inRange("smoothness", 0f, 10f),
        filling
    )
)

data class LocalOrientation(
    val smoothness: Float = 0f,
    val minimumThreshold: Float = 0f,
    val maximumThreshold: Float = 100f,
    val negativeColors: Boolean = false,
    val channel: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_local_orientation",
        smoothness.inRange("smoothness", 0f, 5f),
        minimumThreshold.inRange("minimumThreshold", 0f, 100f),
        maximumThreshold.inRange("maximumThreshold", 0f, 100f),
        negativeColors,
        channel
    )
)

enum class SkeletonMethod(override val value: Int) : GmicArgument {
    Distance(0),
    Thinning(1)
}

data class Skeleton(
    val method: SkeletonMethod = SkeletonMethod.Distance,
    val smoothness: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_skeleton",
        method,
        smoothness.inRange("smoothness", 0f, 10f)
    )
)

enum class SuperPixelColors(override val value: Int) : GmicArgument {
    Random(0),
    Average(1)
}

data class SuperPixels(
    val size: Int = 16,
    val regularity: Float = 10f,
    val iterations: Int = 5,
    val colors: SuperPixelColors = SuperPixelColors.Average,
    val borderOpacity: Float = 1f,
    val borderThickness: Int = 1,
    @param:ColorInt val borderColor: Int = Color.BLACK
) : RawGmicFilter(
    gmicCommand(
        "fx_superpixels",
        size.inRange("size", 4..64),
        regularity.inRange("regularity", 0f, 128f),
        iterations.inRange("iterations", 1..16),
        colors,
        borderOpacity.inRange("borderOpacity", 0f, 1f),
        borderThickness.inRange("borderThickness", 1..16),
        borderColor.gmicRed(),
        borderColor.gmicGreen(),
        borderColor.gmicBlue(),
        borderColor.gmicAlpha()
    ),
    gmicProcessAlpha
)
