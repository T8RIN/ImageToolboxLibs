package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

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

enum class DrosteMirror(override val value: Int) : GmicArgument {
    None(0),
    XAxis(1),
    YAxis(2),
    BothAxes(3)
}

enum class DrosteDrawingMode(override val value: Int) : GmicArgument {
    Replace(0),
    ReplaceSharpest(1),
    Behind(2),
    Below(3)
}

data class Droste(
    val upperLeftX: Float = 20f,
    val upperLeftY: Float = 20f,
    val upperRightX: Float = 80f,
    val upperRightY: Float = 20f,
    val lowerRightX: Float = 80f,
    val lowerRightY: Float = 80f,
    val lowerLeftX: Float = 20f,
    val lowerLeftY: Float = 80f,
    val iterations: Int = 1,
    val xShift: Float = 0f,
    val yShift: Float = 0f,
    val angle: Float = 0f,
    val zoom: Float = 1f,
    val mirror: DrosteMirror = DrosteMirror.None,
    val boundary: GmicBoundary = GmicBoundary.Nearest,
    val drawingMode: DrosteDrawingMode = DrosteDrawingMode.Replace
) : RawGmicFilter(
    gmicCommand(
        "fx_droste",
        upperLeftX.inRange("upperLeftX", 0f, 100f),
        upperLeftY.inRange("upperLeftY", 0f, 100f),
        upperRightX.inRange("upperRightX", 0f, 100f),
        upperRightY.inRange("upperRightY", 0f, 100f),
        lowerRightX.inRange("lowerRightX", 0f, 100f),
        lowerRightY.inRange("lowerRightY", 0f, 100f),
        lowerLeftX.inRange("lowerLeftX", 0f, 100f),
        lowerLeftY.inRange("lowerLeftY", 0f, 100f),
        iterations.inRange("iterations", 1..10),
        xShift.inRange("xShift", -100f, 100f),
        yShift.inRange("yShift", -100f, 100f),
        angle.inRange("angle", 0f, 360f),
        zoom.inRange("zoom", 0.1f, 5f),
        mirror,
        boundary,
        drawingMode
    ),
    gmicProcessAlpha
)

enum class ConformalMapping(override val value: Int) : GmicArgument {
    Identity(1),
    Mobius(2),
    Cosine(3),
    Sine(4),
    Tangent(5),
    Exponential(6),
    Logarithm(7),
    Dipole(8),
    Star(9)
}

data class ConformalMap(
    val mapping: ConformalMapping = ConformalMapping.Dipole,
    val exponentReal: Float = 1f,
    val exponentImaginary: Float = 0f,
    val zoom: Float = 0f,
    val angle: Float = 0f,
    val aspectRatio: Float = 0f,
    val xShift: Float = 0f,
    val yShift: Float = 0f,
    val boundary: GmicBoundary = GmicBoundary.Mirror,
    val antialiasing: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_conformal_maps",
        mapping,
        exponentReal.inRange("exponentReal", -16f, 16f),
        exponentImaginary.inRange("exponentImaginary", -16f, 16f),
        gmicQuoted("z"),
        zoom.inRange("zoom", -4f, 4f),
        angle.inRange("angle", -180f, 180f),
        aspectRatio.inRange("aspectRatio", -1f, 1f),
        xShift.inRange("xShift", -5f, 5f),
        yShift.inRange("yShift", -5f, 5f),
        boundary,
        antialiasing.inRange("antialiasing", 0..3),
        false,
        0,
        0
    ),
    gmicProcessAlpha
)

enum class PoincareTiling(override val value: Int) : GmicArgument {
    Triangular(0),
    Polygonal(1)
}

enum class PoincareFilling(override val value: Int) : GmicArgument {
    Binary(0),
    Image(1)
}

data class PoincareDisk(
    val pValue: Int = 5,
    val qValue: Int = 6,
    val size: Float = 100f,
    val iterations: Int = 20,
    val angle: Float = 0f,
    val tiling: PoincareTiling = PoincareTiling.Polygonal,
    val antialiasing: Boolean = true,
    val filling: PoincareFilling = PoincareFilling.Image,
    val shiftX: Float = 50f,
    val shiftY: Float = 50f,
    val zoom: Float = 0f,
    val imageAngle: Float = 0f,
    val outline: Int = 2,
    @param:ColorInt val outlineColor: Int = Color.BLACK,
    @param:ColorInt val firstColor: Int = Color.BLACK,
    @param:ColorInt val secondColor: Int = Color.WHITE
) : RawGmicFilter(
    gmicCommand(
        "fx_poincare_disk",
        pValue.inRange("pValue", 3..16),
        qValue.inRange("qValue", 3..32),
        size.inRange("size", 0f, 200f),
        iterations.inRange("iterations", 0..40),
        angle.inRange("angle", -180f, 180f),
        tiling,
        antialiasing,
        filling,
        shiftX.inRange("shiftX", 0f, 100f),
        shiftY.inRange("shiftY", 0f, 100f),
        zoom.inRange("zoom", -10f, 10f),
        imageAngle.inRange("imageAngle", -180f, 180f),
        outline.inRange("outline", 0..8),
        outlineColor.gmicRed(),
        outlineColor.gmicGreen(),
        outlineColor.gmicBlue(),
        firstColor.gmicRed(),
        firstColor.gmicGreen(),
        firstColor.gmicBlue(),
        secondColor.gmicRed(),
        secondColor.gmicGreen(),
        secondColor.gmicBlue()
    ),
    gmicProcessAlpha
)

enum class StereographicTransform(override val value: Int) : GmicArgument {
    Direct(0),
    Inverse(1)
}

data class StereographicProjection(
    val transform: StereographicTransform = StereographicTransform.Direct,
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val radiusPointX: Float = 50f,
    val radiusPointY: Float = 75f,
    val horizonLeveling: Float = 0f,
    val sideBlur: Float = 0f,
    val dilation: Float = 0f,
    val mirror: DrosteMirror = DrosteMirror.None,
    val boundary: GmicBoundary = GmicBoundary.Transparent
) : RawGmicFilter(
    gmicCommand(
        "fx_project_stereographic",
        transform,
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        radiusPointX.inRange("radiusPointX", 0f, 100f),
        radiusPointY.inRange("radiusPointY", 0f, 100f),
        horizonLeveling.inRange("horizonLeveling", -10f, 10f),
        sideBlur.inRange("sideBlur", 0f, 20f),
        dilation.inRange("dilation", -2f, 2f),
        mirror,
        boundary,
        centerX,
        centerY
    ),
    gmicProcessAlpha
)

data class Tunnel(
    val depth: Int = 4,
    val factor: Float = 80f,
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val opacity: Float = 0.2f,
    val angle: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_tunnel",
        depth.inRange("depth", 1..100),
        factor.inRange("factor", 1f, 99f),
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        opacity.inRange("opacity", 0f, 1f),
        angle.inRange("angle", -90f, 90f)
    )
)

data class Puzzle(
    val xTiles: Int = 5,
    val yTiles: Int = 5,
    val curvature: Float = 0.5f,
    val connectorCentering: Float = 0f,
    val connectorVariability: Float = 0f,
    val reliefSmoothness: Float = 0.3f,
    val reliefContrast: Float = 100f,
    val outlineSmoothness: Float = 0.2f,
    val outlineContrast: Float = 255f,
    val scale: Float = 100f,
    val scaleVariation: Float = 0f,
    val angle: Float = 0f,
    val angleVariation: Float = 0f,
    val shufflePieces: Boolean = false,
    val additionalOutline: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_puzzle",
        xTiles.inRange("xTiles", 2..32),
        yTiles.inRange("yTiles", 2..32),
        curvature.inRange("curvature", 0f, 1.5f),
        connectorCentering.inRange("connectorCentering", 0f, 1f),
        connectorVariability.inRange("connectorVariability", 0f, 2f),
        reliefSmoothness.inRange("reliefSmoothness", 0f, 3f),
        reliefContrast.inRange("reliefContrast", 0f, 255f),
        outlineSmoothness.inRange("outlineSmoothness", 0f, 3f),
        outlineContrast.inRange("outlineContrast", 0f, 255f),
        scale.inRange("scale", 0f, 150f),
        scaleVariation.inRange("scaleVariation", 0f, 100f),
        angle.inRange("angle", -180f, 180f),
        angleVariation.inRange("angleVariation", 0f, 180f),
        shufflePieces,
        additionalOutline,
        false
    ),
    gmicProcessAlpha
)
