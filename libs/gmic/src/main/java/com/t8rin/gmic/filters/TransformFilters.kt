package com.t8rin.gmic.filters

data class LensDistortion(
    val amplitude: Float = 0.1f,
    val aspectRatio: Float = 0f,
    val zoom: Float = 0f,
    val centerXPercent: Float = 50f,
    val centerYPercent: Float = 50f,
    val boundary: GmicBoundary = GmicBoundary.Transparent
) : RawGmicFilter(
    gmicCommand(
        "fx_distort_lens",
        amplitude.inRange("amplitude", -1f, 1f),
        aspectRatio.inRange("aspectRatio", -2f, 2f),
        zoom.inRange("zoom", -4f, 4f),
        centerXPercent.inRange("centerXPercent", 0f, 100f),
        centerYPercent.inRange("centerYPercent", 0f, 100f),
        boundary
    ),
    gmicProcessAlpha
)

enum class PowerTwirlMode(override val value: Int) : GmicArgument {
    Polar(0),
    Cartesian(1)
}

data class PowerTwirl(
    val amplitude: Float = 1f,
    val offset: Float = 0f,
    val centerXPercent: Float = 50f,
    val centerYPercent: Float = 50f,
    val power: Float = 1f,
    val boundary: GmicBoundary = GmicBoundary.Mirror,
    val mode: PowerTwirlMode = PowerTwirlMode.Polar
) : RawGmicFilter(
    gmicCommand(
        "fx_powertwirl",
        amplitude.inRange("amplitude", -20f, 20f),
        offset.inRange("offset", -180f, 180f),
        centerXPercent.inRange("centerXPercent", 0f, 100f),
        centerYPercent.inRange("centerYPercent", 0f, 100f),
        power.inRange("power", -20f, 20f),
        boundary,
        mode
    ),
    gmicProcessAlpha
)

enum class RandomDeformationsInterpolation(override val value: Int) : GmicArgument {
    NoneOption(0),
    Linear(1),
    Bicubic(2)
}

enum class RandomDeformationsMatrixInterpolation(override val value: Int) : GmicArgument {
    Linear(0),
    Bicubic(1)
}

enum class RandomDeformationsMode(override val value: Int) : GmicArgument {
    Noise(0),
    SpreadNoise(1)
}

enum class RandomDeformationsBoundary(override val value: Int) : GmicArgument {
    Dirichlet(0),
    Neumann(1),
    Periodic(2),
    Mirror(3)
}

data class RandomDeformations(
    val recompute: Boolean = false,
    val amplitude: Float = 5f,
    val interpolation: RandomDeformationsInterpolation = RandomDeformationsInterpolation.Linear,
    val matrixDensity: Float = 10f,
    val matrixInterpolation: RandomDeformationsMatrixInterpolation = RandomDeformationsMatrixInterpolation.Bicubic,
    val mode: RandomDeformationsMode = RandomDeformationsMode.Noise,
    val character: Float = 0f,
    val boundary: RandomDeformationsBoundary = RandomDeformationsBoundary.Neumann
) : RawGmicFilter(
    gmicCommand(
        "fx_jr_deform",
        recompute,
        amplitude.inRange("amplitude", 0f, 50f),
        interpolation,
        matrixDensity.inRange("matrixDensity", 1f, 100f),
        matrixInterpolation,
        mode,
        character.inRange("character", -100f, 100f),
        boundary
    ),
    gmicProcessAlpha
)

data class ShockWaves(
    val amplitude: Float = 10f,
    val lowFrequency: Float = 10f,
    val frequencyRange: Float = 20f,
    val channelS: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_shockwaves",
        amplitude.inRange("amplitude", 0f, 100f),
        lowFrequency.inRange("lowFrequency", 0f, 100f),
        frequencyRange.inRange("frequencyRange", 0f, 100f),
        channelS
    ),
    gmicProcessAlpha
)

enum class RowShifterOrder(override val value: Int) : GmicArgument {
    XThenY(0),
    Simultaneous(1),
    YThenX(2)
}

enum class RowShifterInterpolation(override val value: Int) : GmicArgument {
    NoneOption(0),
    Nearest(1),
    Average(2),
    Bilinear(3),
    Grid(4),
    Bicubic(5),
    Lanczos(6)
}

data class RowShifter(
    val order: RowShifterOrder = RowShifterOrder.XThenY,
    val xShift: Float = 0f,
    val xCentre: Float = 0.5f,
    val yShift: Float = 0f,
    val yCentre: Float = 0.5f,
    val interpolation: RowShifterInterpolation = RowShifterInterpolation.Bilinear,
    val invertShifts: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_row_shift",
        order,
        xShift.inRange("xShift", -5f, 5f),
        xCentre.inRange("xCentre", 0f, 1f),
        yShift.inRange("yShift", -5f, 5f),
        yCentre.inRange("yCentre", 0f, 1f),
        interpolation,
        invertShifts
    ),
    gmicProcessAlpha
)

enum class PolarTransformPreset(override val value: Int) : GmicArgument {
    CustomTransform(0),
    InverseRadius(1),
    SwapRadiusAngle(2)
}

data class PolarTransform(
    val preset: PolarTransformPreset = PolarTransformPreset.CustomTransform,
    val centerXPercent: Float = 50f,
    val centerYPercent: Float = 50f,
    val radiusExpression: String = "r + R/10*cos(a*5)",
    val angleExpression: String = "a",
    val boundary: GmicBoundary = GmicBoundary.Mirror
) : RawGmicFilter(
    gmicCommand(
        "fx_transform_polar",
        preset,
        centerXPercent.inRange("centerXPercent", 0f, 100f),
        centerYPercent.inRange("centerYPercent", 0f, 100f),
        gmicQuoted(radiusExpression),
        gmicQuoted(angleExpression),
        boundary
    ),
    gmicProcessAlpha
)

data class FishEye(
    val radius: Float = 70f,
    val amplitude: Float = 1f
) : RawGmicFilter(
    gmicCommand(
        "fisheye",
        radius.inRange("radius", 0f, 100f),
        amplitude.inRange("amplitude", 0f, 2f)
    ),
    gmicProcessAlpha
)
