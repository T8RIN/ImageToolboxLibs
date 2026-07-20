package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

enum class CubeFaceOrientation(override val value: Int) : GmicArgument {
    Normal(0),
    MirrorX(1),
    MirrorY(2),
    MirrorXY(3)
}

data class CubeFrame(
    val depth: Float = 3f,
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val leftOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val rightOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val upperOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val lowerOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_cube",
        depth.inRange("depth", 0f, 30f),
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        leftOrientation,
        rightOrientation,
        upperOrientation,
        lowerOrientation
    ),
    gmicProcessAlpha
)

enum class FrameNormalization(override val value: Int) : GmicArgument {
    None(0),
    Stretch(1),
    Equalize(2)
}

data class BlurredFrame(
    val horizontalSize: Float = 30f,
    val verticalSize: Float = 30f,
    val crop: Float = 0f,
    val blur: Float = 5f,
    val roundness: Float = 0f,
    val applyColorBalance: Boolean = false,
    @param:ColorInt val balanceColor: Int = Color.rgb(128, 128, 128),
    val normalization: FrameNormalization = FrameNormalization.None,
    val outlineSize: Float = 5f,
    @param:ColorInt val outlineColor: Int = Color.WHITE,
    val xShadow: Float = 2f,
    val yShadow: Float = 2f,
    val shadowSmoothness: Float = 1f,
    val shadowContrast: Float = 0f,
    val xCentering: Float = 0.5f,
    val yCentering: Float = 0.5f,
    val angle: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_blur",
        horizontalSize.inRange("horizontalSize", 0f, 100f),
        verticalSize.inRange("verticalSize", 0f, 100f),
        crop.inRange("crop", 0f, 100f),
        blur.inRange("blur", 0f, 10f),
        roundness.inRange("roundness", 0f, 1f),
        applyColorBalance,
        balanceColor.gmicRed(),
        balanceColor.gmicGreen(),
        balanceColor.gmicBlue(),
        normalization,
        outlineSize.inRange("outlineSize", 0f, 50f),
        outlineColor.gmicRed(),
        outlineColor.gmicGreen(),
        outlineColor.gmicBlue(),
        xShadow.inRange("xShadow", -10f, 10f),
        yShadow.inRange("yShadow", -10f, 10f),
        shadowSmoothness.inRange("shadowSmoothness", 0f, 5f),
        shadowContrast.inRange("shadowContrast", 0f, 100f),
        xCentering.inRange("xCentering", 0f, 1f),
        yCentering.inRange("yCentering", 0f, 1f),
        angle.inRange("angle", -180f, 180f)
    ),
    gmicProcessAlpha
)

data class FuzzyFrame(
    val horizontalSize: Float = 5f,
    val verticalSize: Float = 5f,
    val fuzziness: Float = 10f,
    val smoothness: Float = 1f,
    @param:ColorInt val color: Int = Color.WHITE
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_fuzzy",
        horizontalSize.inRange("horizontalSize", 0f, 100f),
        verticalSize.inRange("verticalSize", 0f, 100f),
        fuzziness.inRange("fuzziness", 0f, 40f),
        smoothness.inRange("smoothness", 0f, 5f),
        color.gmicRed(),
        color.gmicGreen(),
        color.gmicBlue(),
        color.gmicAlpha()
    ),
    gmicProcessAlpha
)

data class MirrorFrame(
    val horizontalSize: Float = 10f,
    val verticalSize: Float = 10f,
    val horizontalAlignment: Float = 50f,
    val verticalAlignment: Float = 50f,
    val leftDilation: Float = 0f,
    val rightDilation: Float = 0f,
    val upperDilation: Float = 0f,
    val lowerDilation: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_mirror",
        horizontalSize.inRange("horizontalSize", 0f, 100f),
        verticalSize.inRange("verticalSize", 0f, 100f),
        horizontalAlignment.inRange("horizontalAlignment", 0f, 100f),
        verticalAlignment.inRange("verticalAlignment", 0f, 100f),
        leftDilation.inRange("leftDilation", -5f, 5f),
        rightDilation.inRange("rightDilation", -5f, 5f),
        upperDilation.inRange("upperDilation", -5f, 5f),
        lowerDilation.inRange("lowerDilation", -5f, 5f)
    ),
    gmicProcessAlpha
)

data class PaintingFrame(
    val size: Float = 10f,
    val contrast: Float = 0.4f,
    val smoothness: Float = 6f,
    @param:ColorInt val color: Int = Color.rgb(225, 200, 120),
    val vignetteSize: Float = 2f,
    val vignetteContrast: Float = 400f,
    val defectsContrast: Float = 50f,
    val defectsDensity: Float = 10f,
    val defectsSize: Float = 1f,
    val defectsSmoothness: Float = 0.5f,
    val serialNumber: Int = 123456
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_painting",
        size.inRange("size", 0f, 100f),
        contrast.inRange("contrast", 0f, 1f),
        smoothness.inRange("smoothness", 0f, 30f),
        color.gmicRed(),
        color.gmicGreen(),
        color.gmicBlue(),
        vignetteSize.inRange("vignetteSize", 0f, 50f),
        vignetteContrast.inRange("vignetteContrast", 0f, 1000f),
        defectsContrast.inRange("defectsContrast", 0f, 512f),
        defectsDensity.inRange("defectsDensity", 0f, 100f),
        defectsSize.inRange("defectsSize", 0f, 10f),
        defectsSmoothness.inRange("defectsSmoothness", 0f, 20f),
        serialNumber.inRange("serialNumber", 0..1000000),
        false
    ),
    gmicProcessAlpha
)

data class RoundedFrame(
    val xSize: Float = 10f,
    val ySize: Float = 10f,
    val radius: Float = 20f,
    val smoothness: Float = 0.1f,
    @param:ColorInt val color: Int = Color.BLACK,
    val antialias: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_round",
        xSize.inRange("xSize", 0f, 100f),
        ySize.inRange("ySize", 0f, 100f),
        radius.inRange("radius", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 15f),
        color.gmicRed(),
        color.gmicGreen(),
        color.gmicBlue(),
        color.gmicAlpha(),
        antialias
    ),
    gmicProcessAlpha
)

data class SmoothFrame(
    val width: Int = 5,
    val height: Int = 5,
    val roundness: Float = 0.25f
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_smooth",
        width.inRange("width", 0..100),
        height.inRange("height", 0..100),
        roundness.inRange("roundness", 0f, 1f)
    )
)
