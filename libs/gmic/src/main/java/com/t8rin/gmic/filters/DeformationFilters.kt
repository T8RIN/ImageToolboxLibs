package com.t8rin.gmic.filters

enum class BreaksType(override val value: Int) : GmicArgument {
    Flat(0),
    Relief(1)
}

data class Breaks(
    val type: BreaksType = BreaksType.Flat,
    val amplitude: Float = 30f,
    val frequency: Float = 30f,
    val smoothness: Float = 0.5f,
    val boundary: GmicBoundary = GmicBoundary.Mirror
) : RawGmicFilter(
    gmicCommand(
        "fx_breaks",
        type,
        amplitude.inRange("amplitude", 0f, 300f),
        frequency.inRange("frequency", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 10f),
        boundary
    ),
    gmicProcessAlpha
)

enum class CircleTransformSymmetry(override val value: Int) : GmicArgument {
    None(0),
    Inside(1),
    Outside(2)
}

data class CircleTransform(
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val radiusPointX: Float = 75f,
    val radiusPointY: Float = 50f,
    val xScale: Float = -2f,
    val yScale: Float = -2f,
    val symmetry: CircleTransformSymmetry = CircleTransformSymmetry.None,
    val interpolation: GmicInterpolation = GmicInterpolation.Linear,
    val boundary: GmicBoundary = GmicBoundary.Mirror
) : RawGmicFilter(
    gmicCommand(
        "fx_circle_transform",
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        radiusPointX.inRange("radiusPointX", 0f, 100f),
        radiusPointY.inRange("radiusPointY", 0f, 100f),
        xScale.inRange("xScale", -16f, 16f),
        yScale.inRange("yScale", -16f, 16f),
        symmetry,
        interpolation,
        boundary
    ),
    gmicProcessAlpha
)

data class Crease(
    val amplitude: Float = 30f,
    val frequency: Float = 10f,
    val boundary: GmicBoundary = GmicBoundary.Mirror
) : RawGmicFilter(
    gmicCommand(
        "fx_crease",
        amplitude.inRange("amplitude", 0f, 300f),
        frequency.inRange("frequency", 0f, 100f),
        boundary
    ),
    gmicProcessAlpha
)

data class Flower(
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val amplitudePointX: Float = 75f,
    val amplitudePointY: Float = 50f,
    val petals: Int = 6,
    val offset: Float = 0f,
    val boundary: GmicBoundary = GmicBoundary.Mirror
) : RawGmicFilter(
    gmicCommand(
        "fx_flower",
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        amplitudePointX.inRange("amplitudePointX", 0f, 100f),
        amplitudePointY.inRange("amplitudePointY", 0f, 100f),
        petals.inRange("petals", 2..20),
        offset.inRange("offset", 0f, 100f),
        boundary
    ),
    gmicProcessAlpha
)

enum class SquareCircleMode(override val value: Int) : GmicArgument {
    SquareToCircle(0),
    CircleToSquare(1)
}

data class SquareToCircle(
    val mode: SquareCircleMode = SquareCircleMode.SquareToCircle,
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val strength: Float = 100f,
    val zoom: Float = 0f,
    val angle: Float = 0f,
    val interpolation: GmicInterpolation = GmicInterpolation.Linear,
    val boundary: GmicBoundary = GmicBoundary.Transparent,
    val adaptToImageRatio: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_square_circle_alt",
        mode,
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        strength.inRange("strength", 0f, 100f),
        zoom.inRange("zoom", -100f, 100f),
        angle.inRange("angle", -180f, 180f),
        interpolation,
        boundary,
        adaptToImageRatio
    ),
    gmicProcessAlpha
)

data class WarpByIntensity(
    val xFactor: Float = 0.04f,
    val yFactor: Float = 0.04f,
    val xOffset: Float = 128f,
    val yOffset: Float = 128f,
    val correlatedChannels: Boolean = false,
    val interpolation: GmicInterpolation = GmicInterpolation.Linear,
    val boundary: GmicBoundary = GmicBoundary.Mirror,
    val channel: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_warp_by_intensity",
        xFactor.inRange("xFactor", -6f, 6f),
        yFactor.inRange("yFactor", -6f, 6f),
        xOffset.inRange("xOffset", 0f, 255f),
        yOffset.inRange("yOffset", 0f, 255f),
        correlatedChannels,
        interpolation,
        boundary,
        channel
    ),
    gmicProcessAlpha
)
