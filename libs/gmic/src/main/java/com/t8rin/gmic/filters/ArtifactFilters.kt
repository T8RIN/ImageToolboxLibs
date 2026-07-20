package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

enum class AberrationType(override val value: Int) : GmicArgument {
    Shift(0),
    Radial(1),
    Angular(2),
    Random(3)
}

data class ChromaticAberrations(
    @param:ColorInt val primaryColor: Int = Color.RED,
    val primaryType: AberrationType = AberrationType.Shift,
    val primaryXAmplitude: Float = 2f,
    val primaryYAmplitude: Float = 2f,
    val primarySmoothness: Float = 0f,
    val primaryCenterAttenuation: Float = 50f,
    val primaryAttenuationDecay: Float = 1f,
    @param:ColorInt val secondaryColor: Int = Color.GREEN,
    val secondaryType: AberrationType = AberrationType.Shift,
    val secondaryXAmplitude: Float = 0f,
    val secondaryYAmplitude: Float = 0f,
    val secondarySmoothness: Float = 0f,
    val secondaryCenterAttenuation: Float = 0f,
    val secondaryAttenuationDecay: Float = 1f
) : RawGmicFilter(
    gmicCommand(
        "fx_chromatic_aberrations",
        primaryColor.gmicRed(),
        primaryColor.gmicGreen(),
        primaryColor.gmicBlue(),
        primaryType,
        primaryXAmplitude.inRange("primaryXAmplitude", -32f, 32f),
        primaryYAmplitude.inRange("primaryYAmplitude", -32f, 32f),
        primarySmoothness.inRange("primarySmoothness", 0f, 10f),
        primaryCenterAttenuation.inRange("primaryCenterAttenuation", -100f, 100f),
        primaryAttenuationDecay.inRange("primaryAttenuationDecay", 0f, 8f),
        secondaryColor.gmicRed(),
        secondaryColor.gmicGreen(),
        secondaryColor.gmicBlue(),
        secondaryType,
        secondaryXAmplitude.inRange("secondaryXAmplitude", -32f, 32f),
        secondaryYAmplitude.inRange("secondaryYAmplitude", -32f, 32f),
        secondarySmoothness.inRange("secondarySmoothness", 0f, 10f),
        secondaryCenterAttenuation.inRange("secondaryCenterAttenuation", -100f, 100f),
        secondaryAttenuationDecay.inRange("secondaryAttenuationDecay", 0f, 8f)
    )
)

enum class CrtPhosphorType(override val value: Int) : GmicArgument {
    Stripes(0),
    Triad(1),
    SlotMask(2)
}

enum class CrtPrecision(override val value: Int) : GmicArgument {
    Low(0),
    High(1)
}

data class CrtPhosphors(
    val type: CrtPhosphorType = CrtPhosphorType.Stripes,
    val upscaleFactor: Int = 4,
    val precision: CrtPrecision = CrtPrecision.Low,
    val smoothness: Float = 0f,
    val neighborhoodSize: Int = 4,
    val stride: Float = 50f,
    val adaptivePattern: Boolean = true,
    val useLuma: Boolean = false,
    val transposePattern: Boolean = false,
    val averageOverPattern: Boolean = false,
    val normalizeImage: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_crt_phosphors",
        type,
        upscaleFactor.inRange("upscaleFactor", 1..8) - 1,
        precision,
        smoothness.inRange("smoothness", 0f, 10f),
        neighborhoodSize.inRange("neighborhoodSize", 1..16),
        stride.inRange("stride", 0f, 100f),
        adaptivePattern,
        useLuma,
        transposePattern,
        averageOverPattern,
        normalizeImage
    )
)

enum class HuffmanSplitMode(override val value: Int) : GmicArgument {
    None(0),
    HorizontalBlocks(1),
    VerticalBlocks(2),
    Patches(3)
}

enum class HuffmanColorSpace(override val value: Int) : GmicArgument {
    Rgb(0),
    Cmyk(1),
    Hcy(2),
    Hsi(3),
    Hsl(4),
    Hsv(5),
    Jzazbz(6),
    Lab(7),
    Lch(8),
    OkLab(9),
    YCbCr(10),
    Yiq(11)
}

data class HuffmanGlitch(
    val noiseLevel: Float = 30f,
    val splitMode: HuffmanSplitMode = HuffmanSplitMode.None,
    val blockSize: Int = 25,
    val patchOverlap: Float = 0f,
    val colorSpace: HuffmanColorSpace = HuffmanColorSpace.Rgb,
    val quantization: Int = 0,
    val randomSeed: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_huffman_glitches",
        noiseLevel.inRange("noiseLevel", 0f, 100f),
        splitMode,
        blockSize.inRange("blockSize", 0..100),
        patchOverlap.inRange("patchOverlap", 0f, 50f),
        colorSpace,
        quantization.inRange("quantization", 0..64),
        randomSeed.inRange("randomSeed", 0..65536)
    )
)

data class JpegArtifacts(
    val quality: Int = 50
) : RawGmicFilter(
    gmicCommand("fx_jpeg_artefacts", quality.inRange("quality", 1..100))
)

enum class PixelSortOrder(override val value: Int) : GmicArgument {
    Decreasing(0),
    Increasing(1)
}

enum class PixelSortAxis(override val value: Int) : GmicArgument {
    X(0),
    Y(1),
    XThenY(2),
    YThenX(3)
}

enum class PixelSortCriterion(override val value: Int) : GmicArgument {
    Red(0),
    Green(1),
    Blue(2),
    Intensity(3),
    Luminance(4),
    Lightness(5),
    Hue(6),
    Saturation(7),
    Minimum(8),
    Maximum(9),
    Random(10)
}

enum class PixelSortMask(override val value: Int) : GmicArgument {
    Criterion(1),
    Contours(2),
    Random(3)
}

data class PixelSort(
    val order: PixelSortOrder = PixelSortOrder.Increasing,
    val axis: PixelSortAxis = PixelSortAxis.X,
    val criterion: PixelSortCriterion = PixelSortCriterion.Red,
    val mask: PixelSortMask = PixelSortMask.Criterion,
    val lowerThreshold: Float = 0f,
    val upperThreshold: Float = 100f,
    val maskSmoothness: Float = 0f,
    val invertMask: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_pixelsort",
        order,
        axis,
        criterion,
        mask,
        lowerThreshold.inRange("lowerThreshold", 0f, 100f),
        upperThreshold.inRange("upperThreshold", 0f, 100f),
        maskSmoothness.inRange("maskSmoothness", 0f, 5f),
        invertMask
    ).also {
        require(lowerThreshold <= upperThreshold) {
            "lowerThreshold must not be greater than upperThreshold"
        }
    }
)

data class RainAndSnow(
    val angle: Float = 65f,
    val speed: Float = 10f,
    val density: Float = 50f,
    val radius: Float = 0.1f,
    val gamma: Float = 1f,
    val opacity: Float = 1f
) : RawGmicFilter(
    buildString {
        append(
            gmicCommand(
                "fx_rain",
                angle.inRange("angle", -180f, 180f),
                speed.inRange("speed", 0f, 50f),
                density.inRange("density", 0f, 100f),
                radius.inRange("radius", 0f, 3f),
                gamma.inRange("gamma", 0f, 2f),
                opacity.inRange("opacity", 0f, 1f)
            )
        )
        append(" rv blend screen,")
        append(opacity)
    }
)

enum class StreakPropagation(override val value: Int) : GmicArgument {
    Backward(0),
    Forward(1),
    BidirectionalSharp(2),
    BidirectionalSmooth(3)
}

data class Streak(
    @param:ColorInt val maskColor: Int = Color.RED,
    val step: Float = 0f,
    val angle: Float = 0f,
    val propagation: StreakPropagation = StreakPropagation.BidirectionalSmooth
) : RawGmicFilter(
    gmicCommand(
        "fx_streak",
        maskColor.gmicRed(),
        maskColor.gmicGreen(),
        maskColor.gmicBlue(),
        maskColor.gmicAlpha(),
        step.inRange("step", 0f, 30f),
        angle.inRange("angle", 0f, 360f),
        propagation
    )
)
