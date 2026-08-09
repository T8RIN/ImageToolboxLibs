package com.t8rin.gmic.filters

data class PolaroidFrame(
    val frameSize: Int = 10,
    val bottomSize: Int = 20,
    val shadowX: Float = 2f,
    val shadowY: Float = 3f,
    val shadowSmoothness: Float = 3f,
    val curvatureX: Float = 0f,
    val curvatureY: Float = 0f,
    val angle: Float = 8f,
    val vignetteStrength: Float = 50f,
    val vignetteMinRadius: Float = 70f,
    val vignetteMaxRadius: Float = 95f
) : RawGmicFilter(
    gmicCommand(
        "fx_polaroid",
        frameSize.inRange("frameSize", 0..400),
        bottomSize.inRange("bottomSize", 0..400),
        shadowX.inRange("shadowX", -20f, 20f),
        shadowY.inRange("shadowY", -20f, 20f),
        shadowSmoothness.inRange("shadowSmoothness", 0f, 5f),
        curvatureX.inRange("curvatureX", 0f, 1f),
        curvatureY.inRange("curvatureY", 0f, 1f),
        angle.inRange("angle", -180f, 180f),
        vignetteStrength.inRange("vignetteStrength", 0f, 255f),
        vignetteMinRadius.inRange("vignetteMinRadius", 0f, 100f),
        vignetteMaxRadius.inRange("vignetteMaxRadius", 0f, 100f)
    )
)

data class PenDrawing(
    val amplitude: Float = 10f
) : RawGmicFilter(
    gmicCommand(
        "fx_pen_drawing",
        amplitude.inRange("amplitude", 0f, 30f)
    )
)

data class OldPhotograph(
    val vignetteStrength: Float = 200f,
    val vignetteMinRadius: Float = 50f,
    val vignetteMaxRadius: Float = 85f
) : RawGmicFilter(
    gmicCommand(
        "fx_old_photo",
        vignetteStrength.inRange("vignetteStrength", 0f, 255f),
        vignetteMinRadius.inRange("vignetteMinRadius", 0f, 100f),
        vignetteMaxRadius.inRange("vignetteMaxRadius", 0f, 100f)
    )
)

data class RetroFade(
    val iterations: Int = 20,
    val colors: Int = 6,
    val grain: Float = 40f
) : RawGmicFilter(
    gmicCommand(
        "fx_retrofade",
        iterations.inRange("iterations", 1..64),
        colors.inRange("colors", 2..32),
        grain.inRange("grain", 1f, 100f)
    )
)

data class SixtiesCinema(
    val grain: Float = 10f,
    val noonForMidnight: Boolean = false,
    val geneKelly: Boolean = false,
    val rayHarryhausen: Boolean = false,
    val caryGrant: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_gb_cfx",
        grain.inRange("grain", 0f, 25f),
        noonForMidnight,
        geneKelly,
        rayHarryhausen,
        caryGrant
    )
)

enum class MetallicLookMetal(override val value: Int) : GmicArgument {
    Silver(0),
    Gold(1),
    Copper(2),
    Bronze(3),
    BlueSteel(4)
}

data class MetallicLook(
    val strength: Float = 1f,
    val smoothness: Float = 0f,
    val metal: MetallicLookMetal = MetallicLookMetal.Silver
) : RawGmicFilter(
    gmicCommand(
        "fx_tk_metallic",
        strength.inRange("strength", 0f, 1f),
        smoothness.inRange("smoothness", 0f, 20f),
        metal
    )
)

data class ZoneSystem(
    val shadowsZone: Int = 2,
    val highlightsZone: Int = 9,
    val gamma: Float = 1.1f,
    val contrast: Float = 1.15f,
    val blackPoint: Int = 8,
    val whitePoint: Int = 245
) : RawGmicFilter(
    gmicCommand(
        "fx_zonesystem",
        shadowsZone.inRange("shadowsZone", 1..5),
        highlightsZone.inRange("highlightsZone", 6..10),
        gamma.inRange("gamma", 0f, 5f),
        contrast.inRange("contrast", 0f, 4f),
        blackPoint.inRange("blackPoint", 0..255),
        whitePoint.inRange("whitePoint", 0..255)
    )
)

data class BufferError(
    val width: Float = 50f,
    val height: Float = 50f,
    val bufferSelectionStartPercent: Float = 0f,
    val bufferSelectionLengthPercent: Float = 100f,
    val bufferSelectionShiftPercent: Float = 0f,
    val repeatBuffer: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_buffer_error",
        width.inRange("width", 0f, 200f),
        height.inRange("height", 0f, 200f),
        bufferSelectionStartPercent.inRange("bufferSelectionStartPercent", 0f, 100f),
        bufferSelectionLengthPercent.inRange("bufferSelectionLengthPercent", 0f, 400f),
        bufferSelectionShiftPercent.inRange("bufferSelectionShiftPercent", 0f, 100f),
        repeatBuffer
    )
)

data class FreakyBlackAndWhite(
    val strengthPercent: Float = 90f,
    val oddnessPercent: Float = 20f,
    val brightnessPercent: Float = 0f,
    val contrastPercent: Float = 0f,
    val gammaPercent: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_freaky_bw",
        strengthPercent.inRange("strengthPercent", 0f, 100f),
        oddnessPercent.inRange("oddnessPercent", 0f, 100f),
        brightnessPercent.inRange("brightnessPercent", -100f, 100f),
        contrastPercent.inRange("contrastPercent", -100f, 100f),
        gammaPercent.inRange("gammaPercent", -100f, 100f)
    )
)

data class DodgeSketch(
    val power: Int = 3,
    val spatialVariance: Float = 10f,
    val valueVariance: Float = 7f,
    val iterations: Int = 2,
    val colored: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_dodgesketch",
        power.inRange("power", 0..10),
        spatialVariance.inRange("spatialVariance", 0f, 100f),
        valueVariance.inRange("valueVariance", 0f, 100f),
        iterations.inRange("iterations", 1..10),
        colored
    )
)

enum class PosterizedDitheringMixerMode(override val value: Int) : GmicArgument {
    ColorDoping(0),
    Darken(1),
    SoftLight(2),
    GrainMerge(3),
    Multiply(4),
    Value(5)
}

data class PosterizedDithering(
    val gamma: Float = 1f,
    val contrast: Float = 1f,
    val brightness: Float = 0f,
    val smoothness: Float = 0f,
    val quantizeColors: Int = 20,
    val smoothColors: Float = 1f,
    val mixerMode: PosterizedDitheringMixerMode = PosterizedDitheringMixerMode.ColorDoping,
    val colorIntensity: Float = 1f
) : RawGmicFilter(
    gmicCommand(
        "fx_pdithered",
        gamma.inRange("gamma", 0.01f, 5f),
        contrast.inRange("contrast", 0f, 4f),
        brightness.inRange("brightness", -255f, 255f),
        smoothness.inRange("smoothness", 0f, 10f),
        quantizeColors.inRange("quantizeColors", 2..255),
        smoothColors.inRange("smoothColors", 0f, 30f),
        mixerMode,
        colorIntensity.inRange("colorIntensity", 0f, 1f)
    )
)

data class Hedcut(
    val contrast: Float = 0.5f,
    val luminanceLevel: Float = 0.5f,
    val smoothing: Float = 0.5f,
    val size: Float = 0.0f,
    val step: Float = 0.5f,
    val quality: Boolean = false,
    val forceGray: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "hedcut",
        contrast.inRange("contrast", 0f, 1f),
        luminanceLevel.inRange("luminanceLevel", 0f, 1f),
        smoothing.inRange("smoothing", 0f, 1f),
        size.inRange("size", 0f, 1f),
        step.inRange("step", 0f, 1f),
        quality,
        forceGray
    )
)

enum class HoughSketchColorModel(override val value: Int) : GmicArgument {
    BlackOnWhite(0),
    WhiteOnBlack(1),
    BlackOnTransparentWhite(2),
    WhiteOnTransparentBlack(3),
    ColorOnWhite(4)
}

data class HoughSketch(
    val smoothness: Float = 1.25f,
    val density: Float = 18f,
    val radius: Int = 5,
    val threshold: Float = 70f,
    val opacity: Float = 0.35f,
    val colorModel: HoughSketchColorModel = HoughSketchColorModel.ColorOnWhite
) : RawGmicFilter(
    gmicCommand(
        "fx_houghsketchbw",
        smoothness.inRange("smoothness", 0f, 10f),
        density.inRange("density", 0f, 100f),
        radius.inRange("radius", 0..30),
        threshold.inRange("threshold", 0f, 100f),
        opacity.inRange("opacity", 0f, 1f),
        colorModel
    )
)

data class Warhol(
    val xTiles: Int = 2,
    val yTiles: Int = 2,
    val smoothness: Float = 2f,
    val color: Float = 45f
) : RawGmicFilter(
    gmicCommand(
        "warhol",
        xTiles.inRange("xTiles", 1..10),
        yTiles.inRange("yTiles", 1..10),
        smoothness.inRange("smoothness", 0f, 10f),
        color.inRange("color", 0f, 60f)
    )
)
