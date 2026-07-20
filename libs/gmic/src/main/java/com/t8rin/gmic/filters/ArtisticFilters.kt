package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

data class CircleAbstraction(
    val numberOfColors: Int = 8,
    val density: Int = 5,
    val opacity: Float = 0.8f,
    val smoothness: Float = 0f,
    val filledCircles: Boolean = true,
    val fillTransparentHoles: Boolean = true,
    val normalizeColors: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_circle_abstraction",
        numberOfColors.inRange("numberOfColors", 2..16),
        density.inRange("density", 1..100),
        opacity.inRange("opacity", 0f, 1f),
        smoothness.inRange("smoothness", 0f, 4f),
        filledCircles,
        fillTransparentHoles,
        normalizeColors
    ),
    gmicProcessAlpha
)

data class Cubism(
    val iterations: Int = 2,
    val density: Float = 50f,
    val thickness: Float = 10f,
    val angle: Float = 90f,
    val opacity: Float = 0.7f,
    val smoothness: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_cubism",
        iterations.inRange("iterations", 0..10),
        density.inRange("density", 0f, 200f),
        thickness.inRange("thickness", 0f, 50f),
        angle.inRange("angle", 0f, 360f),
        opacity.inRange("opacity", 0.01f, 1f),
        smoothness.inRange("smoothness", 0f, 5f)
    )
)

data class FeltPen(
    val amplitude: Float = 300f,
    val density: Float = 50f,
    val smoothness: Float = 1f,
    val opacity: Float = 0.1f,
    val edge: Float = 20f,
    val thickness: Int = 5
) : RawGmicFilter(
    gmicCommand(
        "fx_feltpen",
        amplitude.inRange("amplitude", 0f, 4000f),
        density.inRange("density", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 10f),
        opacity.inRange("opacity", 0f, 1f),
        edge.inRange("edge", 0f, 100f),
        thickness.inRange("thickness", 2..32)
    )
)

data class Ghost(
    val amplitude: Float = 200f,
    val smoothness: Float = 2f,
    val coherence: Float = 2f,
    val gamma: Float = 1f,
    val normalizationAmplitude: Float = 3f,
    val normalizationRadius: Float = 16f,
    val invert: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_ghost",
        amplitude.inRange("amplitude", 0f, 1000f),
        smoothness.inRange("smoothness", 0f, 10f),
        coherence.inRange("coherence", 0f, 10f),
        gamma.inRange("gamma", -3f, 3f),
        normalizationAmplitude.inRange("normalizationAmplitude", 0f, 10f),
        normalizationRadius.inRange("normalizationRadius", 1f, 64f),
        invert
    )
)

enum class LinifyColorMode(override val value: Int) : GmicArgument {
    Subtractive(0),
    Additive(1)
}

data class Linify(
    val density: Float = 40f,
    val spreading: Float = 2f,
    val resolution: Float = 40f,
    val lineOpacity: Float = 10f,
    val linePrecision: Int = 24,
    val colorMode: LinifyColorMode = LinifyColorMode.Subtractive
) : RawGmicFilter(
    gmicCommand(
        "fx_linify",
        density.inRange("density", 0f, 100f),
        spreading.inRange("spreading", 0f, 10f),
        resolution.inRange("resolution", 0f, 100f),
        lineOpacity.inRange("lineOpacity", 0f, 30f),
        linePrecision.inRange("linePrecision", 1..128),
        colorMode,
        false
    )
)

enum class MarkerBackground(override val value: Int) : GmicArgument {
    Blur(0),
    Color(1)
}

data class MarkerDrawing(
    val length: Int = 70,
    val radius: Float = 4f,
    val opacity: Float = 10f,
    val inertia: Float = 15f,
    val curviness: Float = 100f,
    val anisotropy: Float = 85f,
    val smoothness: Float = 0.5f,
    val coherence: Float = 3f,
    val iterations: Int = 3,
    val background: MarkerBackground = MarkerBackground.Color,
    val backgroundBlur: Float = 20f,
    @param:ColorInt val backgroundColor: Int = Color.WHITE
) : RawGmicFilter(
    gmicCommand(
        "fx_marker_drawing",
        length.inRange("length", 1..256),
        radius.inRange("radius", 1f, 32f),
        opacity.inRange("opacity", 0f, 100f),
        inertia.inRange("inertia", 0f, 100f),
        curviness.inRange("curviness", 0f, 200f),
        anisotropy.inRange("anisotropy", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 5f),
        coherence.inRange("coherence", 0f, 16f),
        iterations.inRange("iterations", 1..64),
        background,
        backgroundBlur.inRange("backgroundBlur", 0f, 128f),
        backgroundColor.gmicRed(),
        backgroundColor.gmicGreen(),
        backgroundColor.gmicBlue(),
        backgroundColor.gmicAlpha()
    ),
    gmicProcessAlpha
)

enum class RodiliusColorMode(override val value: Int) : GmicArgument {
    Darker(0),
    Lighter(1)
}

data class Rodilius(
    val amplitude: Float = 10f,
    val thickness: Float = 10f,
    val sharpness: Float = 300f,
    val orientations: Int = 5,
    val offset: Float = 30f,
    val smoothness: Int = 0,
    val colorMode: RodiliusColorMode = RodiliusColorMode.Lighter,
    val channel: GmicChannel = GmicChannel.All,
    val valueAction: GmicValueAction = GmicValueAction.None
) : RawGmicFilter(
    gmicCommand(
        "fx_rodilius",
        amplitude.inRange("amplitude", 0f, 30f),
        thickness.inRange("thickness", 0f, 100f),
        sharpness.inRange("sharpness", 0f, 1000f),
        orientations.inRange("orientations", 2..36),
        offset.inRange("offset", 0f, 180f),
        smoothness.inRange("smoothness", 0..5),
        colorMode,
        channel,
        valueAction
    )
)

enum class ShapeismShape(override val value: Int) : GmicArgument {
    Square(0),
    Triangle(1),
    Circle(2),
    Diamond(3),
    Hexagon(4),
    Octagon(5),
    Star(6),
    Heart(7),
    Custom(8)
}

enum class ShapeismAngle(override val value: Int) : GmicArgument {
    Zero(0),
    ZeroOr180(1),
    RightAngles(2),
    Any(3)
}

data class Shapeism(
    val shape: ShapeismShape = ShapeismShape.Circle,
    val branches: Int = 7,
    val thickness: Float = 0.38f,
    val angle: Float = 0f,
    val antialiasing: Boolean = true,
    val scales: Int = 5,
    val maximalSize: Int = 32,
    val minimalSize: Int = 8,
    val allowedAngles: ShapeismAngle = ShapeismAngle.Any,
    val spacing: Int = 0,
    val precision: Int = 5,
    val edges: Float = 0.5f,
    val smoothness: Float = 1f,
    @param:ColorInt val backgroundColor: Int = Color.BLACK
) : RawGmicFilter(
    gmicCommand(
        "fx_shapeism",
        shape,
        branches.inRange("branches", 3..16),
        thickness.inRange("thickness", 0f, 1f),
        angle.inRange("angle", 0f, 360f),
        antialiasing,
        scales.inRange("scales", 1..16),
        maximalSize.inRange("maximalSize", 1..256),
        minimalSize.inRange("minimalSize", 1..256),
        allowedAngles,
        spacing.inRange("spacing", -5..5),
        precision.inRange("precision", 1..10),
        edges.inRange("edges", 0f, 2f),
        smoothness.inRange("smoothness", 0f, 10f),
        backgroundColor.gmicRed(),
        backgroundColor.gmicGreen(),
        backgroundColor.gmicBlue(),
        backgroundColor.gmicAlpha()
    ),
    gmicProcessAlpha
)

data class SharpAbstract(
    val spatialScale: Float = 4f,
    val valueScale: Float = 10f,
    val precision: Float = 0.5f,
    val channel: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_sharp_abstract",
        spatialScale.inRange("spatialScale", 0f, 32f),
        valueScale.inRange("valueScale", 0f, 16f),
        precision.inRange("precision", 0f, 2f),
        channel
    )
)

enum class AbstractRegularization(override val value: Int) : GmicArgument {
    Isotropic(0),
    DelaunayGuided(1),
    EdgeOriented(2)
}

data class SmoothAbstract(
    val smoothness: Float = 75f,
    val regularization: AbstractRegularization = AbstractRegularization.Isotropic,
    val regularizationIterations: Int = 20,
    val geometry: Float = 1f,
    val details: Float = 30f
) : RawGmicFilter(
    gmicCommand(
        "fx_smooth_abstract",
        smoothness.inRange("smoothness", 0f, 100f),
        regularization,
        regularizationIterations.inRange("regularizationIterations", 0..100),
        geometry.inRange("geometry", 0f, 5f),
        details.inRange("details", 0f, 50f)
    )
)

enum class StringLink(override val value: Int) : GmicArgument {
    TwoPoints(0),
    ThreePoints(1),
    FourPoints(2),
    AllPoints(3)
}

data class Stringify(
    val smoothness: Int = 2,
    val levels: Int = 32,
    val samplingRate: Float = 20f,
    val link: StringLink = StringLink.ThreePoints,
    val offset: Float = 100f,
    val threshold: Int = 32,
    val opacity: Float = 20f,
    val normalization: Float = 25f,
    val fillHoles: Boolean = false,
    @param:ColorInt val backgroundColor: Int = Color.BLACK
) : RawGmicFilter(
    gmicCommand(
        "fx_stringify",
        smoothness.inRange("smoothness", 0..10),
        levels.inRange("levels", 2..64),
        samplingRate.inRange("samplingRate", 0f, 100f),
        link,
        offset.inRange("offset", 0f, 100f),
        threshold.inRange("threshold", 0..1024),
        opacity.inRange("opacity", 0f, 100f),
        normalization.inRange("normalization", 0f, 100f),
        fillHoles,
        backgroundColor.gmicRed(),
        backgroundColor.gmicGreen(),
        backgroundColor.gmicBlue()
    )
)

enum class EngraveAntialiasing(override val value: Int) : GmicArgument {
    Disabled(0),
    X1_5(1),
    X2(2),
    X3(3)
}

data class Engrave(
    val radius: Float = 0.5f,
    val density: Float = 50f,
    val edges: Float = 0f,
    val coherence: Float = 8f,
    val threshold: Float = 40f,
    val minimalArea: Int = 0,
    val flatRegionsRemoval: Float = 0f,
    val addColorBackground: Boolean = false,
    val quantization: Float = 10f,
    val shading: Int = 1,
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val lightness: Float = 0f,
    val antialiasing: EngraveAntialiasing = EngraveAntialiasing.X1_5
) : RawGmicFilter(
    gmicCommand(
        "fx_engrave_preview",
        radius.inRange("radius", 0f, 2f),
        density.inRange("density", 0f, 200f),
        edges.inRange("edges", 0f, 10f),
        coherence.inRange("coherence", 0f, 40f),
        threshold.inRange("threshold", 0f, 100f),
        minimalArea.inRange("minimalArea", -256..256),
        flatRegionsRemoval.inRange("flatRegionsRemoval", 0f, 10f),
        addColorBackground,
        quantization.inRange("quantization", 0f, 40f),
        shading.inRange("shading", 0..5),
        hue.inRange("hue", -180f, 180f),
        saturation.inRange("saturation", -100f, 100f),
        lightness.inRange("lightness", -100f, 100f),
        antialiasing
    ),
    gmicProcessAlpha
)

enum class FilamentColorMode(override val value: Int) : GmicArgument {
    WhiteOnBlack(0),
    BlackOnWhite(1),
    WhiteOnTransparent(2),
    BlackOnTransparent(3)
}

data class Filaments(
    val density: Float = 50f,
    val length: Float = 50f,
    val contour: Float = 75f,
    val distortion: Float = 30f,
    val smoothness: Float = 10f,
    val rotation: Float = 0f,
    val localNormalization: Boolean = true,
    val throwFromLeft: Boolean = true,
    val throwFromRight: Boolean = true,
    val throwFromAbove: Boolean = true,
    val throwFromBelow: Boolean = true,
    val opacity: Float = 50f,
    val colorMode: FilamentColorMode = FilamentColorMode.WhiteOnBlack
) : RawGmicFilter(
    gmicCommand(
        "fx_filaments",
        density.inRange("density", 0f, 100f),
        length.inRange("length", 0f, 100f),
        contour.inRange("contour", 0f, 100f),
        distortion.inRange("distortion", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 100f),
        rotation.inRange("rotation", -180f, 180f),
        localNormalization,
        throwFromLeft,
        throwFromRight,
        throwFromAbove,
        throwFromBelow,
        opacity.inRange("opacity", 0f, 100f),
        colorMode
    ),
    gmicProcessAlpha
)

enum class InkWashContrast(override val value: Int) : GmicArgument {
    None(0),
    Automatic(1),
    AutomaticWithContrastMask(2),
    Manual(3)
}

data class InkWash(
    val size: Float = 0.14f,
    val amplitude: Float = 23f,
    val skipOtherSteps: Boolean = false,
    val smootherSharpness: Float = 0.5f,
    val edgeProtection: Float = 0.54f,
    val softness: Float = 2.25f,
    val stretchContrast: InkWashContrast = InkWashContrast.None,
    val localNormalizationAmplitude: Float = 2f,
    val localNormalizationSize: Float = 6f,
    val neighborhoodSmoothness: Float = 5f,
    val averageSmoothness: Float = 20f
) : RawGmicFilter(
    gmicCommand(
        "fx_ink_wash",
        size.inRange("size", 0f, 4f),
        amplitude.inRange("amplitude", 0f, 200f),
        skipOtherSteps,
        smootherSharpness.inRange("smootherSharpness", 0f, 2f),
        edgeProtection.inRange("edgeProtection", 0f, 1f),
        softness.inRange("softness", 0f, 10f),
        stretchContrast,
        localNormalizationAmplitude.inRange("localNormalizationAmplitude", 0f, 60f),
        localNormalizationSize.inRange("localNormalizationSize", 0f, 64f),
        neighborhoodSmoothness.inRange("neighborhoodSmoothness", 0f, 40f),
        averageSmoothness.inRange("averageSmoothness", 0f, 40f)
    )
)

enum class BrushShape(override val value: Int) : GmicArgument {
    Rectangle(2),
    Diamond(3),
    Pentagon(4),
    Hexagon(5),
    Octagon(6),
    Ellipse(7),
    Gaussian(8),
    Star(9),
    Heart(10)
}

enum class BrushLight(override val value: Int) : GmicArgument {
    None(0),
    Flat(1),
    Darken(2),
    Lighten(3),
    Full(4)
}

data class Brushify(
    val shape: BrushShape = BrushShape.Ellipse,
    val ratio: Float = 0.25f,
    val numberOfSizes: Int = 4,
    val maximalSize: Int = 64,
    val minimalSize: Float = 25f,
    val orientations: Int = 12,
    val fuzziness: Float = 0f,
    val smoothness: Float = 2f,
    val light: BrushLight = BrushLight.Full,
    val lightStrength: Float = 0.2f,
    val opacity: Float = 0.5f,
    val density: Float = 30f,
    val contourCoherence: Float = 1f,
    val orientationCoherence: Float = 1f,
    val gradientSmoothness: Float = 1f,
    val structureSmoothness: Float = 5f,
    val primaryAngle: Float = 0f,
    val angleDispersion: Float = 0.2f
) : RawGmicFilter(
    gmicCommand(
        "fx_brushify",
        shape,
        ratio.inRange("ratio", 0f, 1f),
        numberOfSizes.inRange("numberOfSizes", 1..16),
        maximalSize.inRange("maximalSize", 1..128),
        minimalSize.inRange("minimalSize", 0f, 100f),
        orientations.inRange("orientations", 1..24),
        fuzziness.inRange("fuzziness", 0f, 10f),
        smoothness.inRange("smoothness", 0f, 10f),
        light,
        lightStrength.inRange("lightStrength", 0f, 1f),
        opacity.inRange("opacity", 0f, 1f),
        density.inRange("density", 0f, 100f),
        contourCoherence.inRange("contourCoherence", 0f, 1f),
        orientationCoherence.inRange("orientationCoherence", 0f, 1f),
        gradientSmoothness.inRange("gradientSmoothness", 0f, 10f),
        structureSmoothness.inRange("structureSmoothness", 0f, 10f),
        primaryAngle.inRange("primaryAngle", -180f, 180f),
        angleDispersion.inRange("angleDispersion", 0f, 1f)
    )
)

data class Cutout(
    val levels: Int = 4,
    val edgeSimplicity: Float = 0.5f,
    val edgeFidelity: Int = 4,
    val normalize: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_cutout",
        levels.inRange("levels", 2..32),
        edgeSimplicity.inRange("edgeSimplicity", 0f, 3f),
        edgeFidelity.inRange("edgeFidelity", 0..10),
        normalize
    )
)

data class Ellipsionism(
    val primaryRadius: Float = 20f,
    val secondaryRadius: Float = 10f,
    val smoothness: Float = 0.5f,
    val opacity: Float = 0.7f,
    val outline: Float = 3f,
    val density: Float = 0.5f
) : RawGmicFilter(
    gmicCommand(
        "fx_ellipsionism",
        primaryRadius.inRange("primaryRadius", 1f, 100f),
        secondaryRadius.inRange("secondaryRadius", 1f, 100f),
        smoothness.inRange("smoothness", 0f, 10f),
        opacity.inRange("opacity", 0f, 1f),
        outline.inRange("outline", 1f, 3f),
        density.inRange("density", 0.1f, 2f)
    )
)

data class Fractalize(
    val detailLevel: Float = 0.8f
) : RawGmicFilter(
    gmicCommand("fractalize", detailLevel.inRange("detailLevel", 0f, 1f))
)

data class HopePoster(
    val gamma: Float = 0f,
    val smoothness: Float = 3f
) : RawGmicFilter(
    gmicCommand(
        "fx_poster_hope",
        gamma.inRange("gamma", -3f, 3f),
        smoothness.inRange("smoothness", 0f, 20f)
    )
)

data class LylejkPainting(
    val iterations: Int = 10,
    val abstraction: Int = 2,
    val radius: Int = 4,
    val canvas: Float = 10f
) : RawGmicFilter(
    gmicCommand(
        "fx_lylejk_painting",
        iterations.inRange("iterations", 1..20),
        abstraction.inRange("abstraction", 1..20),
        radius.inRange("radius", 1..30),
        canvas.inRange("canvas", 0f, 100f)
    )
)

data class Painting(
    val abstraction: Int = 5,
    val detailsScale: Float = 2.5f,
    val color: Float = 1.5f,
    val smoothness: Float = 50f,
    val sharpenShades: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_painting",
        abstraction.inRange("abstraction", 1..10),
        detailsScale.inRange("detailsScale", 0f, 5f),
        color.inRange("color", 0f, 4f),
        smoothness.inRange("smoothness", 0f, 1000f),
        sharpenShades
    )
)

data class PosterEdges(
    val imageSmoothness: Float = 20f,
    val edgeThreshold: Float = 60f,
    val edgeShade: Float = 5f,
    val edgeThickness: Float = 0f,
    val edgeAntialiasing: Float = 10f,
    val posterizationLevel: Int = 0,
    val posterizationAntialiasing: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_poster_edges",
        imageSmoothness.inRange("imageSmoothness", 0f, 100f),
        edgeThreshold.inRange("edgeThreshold", 0f, 100f),
        edgeShade.inRange("edgeShade", 0f, 30f),
        edgeThickness.inRange("edgeThickness", 0f, 5f),
        edgeAntialiasing.inRange("edgeAntialiasing", 0f, 100f),
        posterizationLevel.inRange("posterizationLevel", 0..15),
        posterizationAntialiasing.inRange("posterizationAntialiasing", 0f, 100f)
    )
)

enum class QuadtreeMode(override val value: Int) : GmicArgument {
    Squares(0),
    Sierpinski(1),
    EllipsePainting(2)
}

data class Quadtree(
    val mode: QuadtreeMode = QuadtreeMode.Squares,
    val precision: Int = 1024,
    val homogeneity: Float = 0.5f,
    val outline: Int = 0,
    val primaryRadius: Float = 3f,
    val secondaryRadius: Float = 1.5f,
    val anisotropy: Float = 1f,
    val onlyLeafs: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_quadtree",
        mode,
        precision.inRange("precision", 2..4096),
        homogeneity.inRange("homogeneity", 0f, 2f),
        outline.inRange("outline", 0..4),
        primaryRadius.inRange("primaryRadius", 0f, 5f),
        secondaryRadius.inRange("secondaryRadius", 0f, 5f),
        anisotropy.inRange("anisotropy", 0f, 4f),
        onlyLeafs
    )
)

data class VectorPainting(
    val details: Float = 9f
) : RawGmicFilter(
    gmicCommand("fx_vector_painting", details.inRange("details", 0f, 10f))
)

data class Cracks(
    val density: Float = 30f,
    val relief: Boolean = true,
    @param:ColorInt val color: Int = 0x80FFFFFF.toInt(),
    val channel: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_cracks",
        density.inRange("density", 0f, 100f),
        relief,
        color.gmicRed(),
        color.gmicGreen(),
        color.gmicBlue(),
        color.gmicAlpha(),
        channel
    )
)

data class MineralMosaic(
    val density: Float = 1f,
    val area: Float = 2f,
    val smoothness: Float = 1f,
    val shadeStrength: Float = 100f,
    val shadeAngle: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_mineral_mosaic",
        density.inRange("density", 0f, 3f),
        area.inRange("area", 0f, 32f),
        smoothness.inRange("smoothness", 0f, 10f),
        shadeStrength.inRange("shadeStrength", 0f, 255f),
        shadeAngle.inRange("shadeAngle", 0f, 360f)
    )
)

data class StainedGlass(
    val edges: Float = 40f,
    val shading: Float = 0.1f,
    val thinSeparators: Boolean = false,
    val equalize: Boolean = true,
    val colors: Float = 1f,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val gamma: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_stained_glass",
        edges.inRange("edges", 0f, 100f),
        shading.inRange("shading", 0f, 0.5f),
        thinSeparators,
        equalize,
        colors.inRange("colors", 0f, 3f),
        brightness.inRange("brightness", -100f, 100f),
        contrast.inRange("contrast", -100f, 100f),
        gamma.inRange("gamma", -100f, 100f)
    )
)
