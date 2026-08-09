package com.t8rin.gmic.filters

data class Unpurple(
    val intensityOfPurpleFringe: Float = 1f,
    val minimumBrightness: Float = 0f,
    val minimumRedBlueRatioInTheFringe: Float = 0f,
    val maximumRedBlueRatioInTheFringe: Float = 0.33f,
    val blurStandardDeviation: Int = 5,
    val gentleModeOverridesMinimumBrightnessAndMinimunRedBlueRatio: Boolean = false,
    val bitDepth: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "unpurple",
        intensityOfPurpleFringe.inRange("intensityOfPurpleFringe", 0f, 1f),
        minimumBrightness.inRange("minimumBrightness", 0f, 1f),
        minimumRedBlueRatioInTheFringe.inRange("minimumRedBlueRatioInTheFringe", 0f, 1f),
        maximumRedBlueRatioInTheFringe.inRange("maximumRedBlueRatioInTheFringe", 0f, 1f),
        blurStandardDeviation.inRange("blurStandardDeviation", 1..10),
        gentleModeOverridesMinimumBrightnessAndMinimunRedBlueRatio,
        bitDepth
    )
)

data class Unstrip(
    val smoothness: Float = 1f,
    val size: Float = 20f,
    val sensitivity: Float = 4f,
    val normalize: Boolean = true,
    val fftPreview: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "jeje_unstrip",
        smoothness.inRange("smoothness", 0f, 10f),
        size.inRange("size", 1f, 50f),
        sensitivity.inRange("sensitivity", 1f, 10f),
        normalize,
        fftPreview
    )
)

enum class BandingDenoiseSizeOfTiles(override val value: Int) : GmicArgument {
    NoTiling(0),
    Px64(1),
    Px128(2),
    Px256(3),
    Px512(4),
    Px1024(5),
    Px2048(6)
}

data class BandingDenoise(
    val vCutoff: Float = 5f,
    val hCutoff: Float = 5f,
    val space: Float = 5f,
    val value: Float = 10f,
    val sizeOfTiles: BandingDenoiseSizeOfTiles = BandingDenoiseSizeOfTiles.NoTiling
) : RawGmicFilter(
    gmicCommand(
        "banding_denoise_v2",
        vCutoff.inRange("vCutoff", 0f, 50f),
        hCutoff.inRange("hCutoff", 0f, 50f),
        space.inRange("space", 0f, 20f),
        value.inRange("value", 0f, 100f),
        sizeOfTiles
    )
)

data class DcpDehaze(
    val scale: Int = 5,
    val strength: Float = 1f,
    val min: Float = 0.2f,
    val max: Float = 1f,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val gamma: Float = 0f,
    val transmittanceMap: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "jeje_dehaze",
        scale.inRange("scale", 1..20),
        strength.inRange("strength", 0f, 2f),
        min.inRange("min", 0f, 1f),
        max.inRange("max", 0f, 1f),
        brightness.inRange("brightness", -100f, 100f),
        contrast.inRange("contrast", -100f, 100f),
        gamma.inRange("gamma", -100f, 100f),
        transmittanceMap
    )
)

data class HessianSharpen(
    val numberOfScales: Int = 3,
    val strength: Float = 1f,
    val repeat: Float = 1f,
    val cut: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "jeje_hessian_sharpen",
        numberOfScales.inRange("numberOfScales", 2..10),
        strength.inRange("strength", 0f, 10f),
        repeat.inRange("repeat", 1f, 5f),
        cut
    )
)

data class WhitenSharpen(
    val alpha: Float = 50f,
    val cut: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "jeje_whiten_frequency",
        alpha.inRange("alpha", 0f, 100f),
        cut
    )
)

data class Descreen(
    val circleRadiusPercent: Float = 7f,
    val crossLengthPercent: Float = 40f,
    val crossWidthPercent: Float = 1f
) : RawGmicFilter(
    gmicCommand(
        "fx_pahlsson_descreen",
        circleRadiusPercent.inRange("circleRadiusPercent", 0f, 100f),
        crossLengthPercent.inRange("crossLengthPercent", 0f, 100f),
        crossWidthPercent.inRange("crossWidthPercent", 0f, 100f)
    )
)

data class DenoiseSmooth(
    val radius: Int = 3,
    val amount: Int = 10
) : RawGmicFilter(
    gmicCommand(
        "afre_denoisesmooth",
        radius.inRange("radius", 1..10),
        amount.inRange("amount", 1..1000)
    )
)

data class CleanText(
    val clean: Int = 8,
    val range: Float = 1f,
    val black: Int = 80,
    val white: Int = 95
) : RawGmicFilter(
    gmicCommand(
        "afre_cleantext",
        clean.inRange("clean", 0..10),
        range.inRange("range", 0.2f, 1f),
        black.inRange("black", 0..100),
        white.inRange("white", 0..100)
    )
)

enum class FillHolesChannelS(override val value: Int) : GmicArgument {
    All(0),
    RGBAAll(1),
    RGBAll(2),
    RGBRed(3),
    RGBGreen(4),
    RGBBlue(5),
    RGBAAlpha(6),
    YCbCrLuminance(7),
    YCbCrBlueRedChrominances(8),
    YCbCrBlueChrominance(9),
    YCbCrRedChrominance(10),
    YCbCrGreenChrominance(11),
    LabLightness(12),
    LabAbChrominances(13),
    LabAChrominance(14),
    LabBChrominance(15),
    LchChChrominances(16),
    LchCChrominance(17),
    LchHChrominance(18),
    HSVHue(19),
    HSVSaturation(20),
    HSVValue(21),
    HSIIntensity(22),
    HSLLightness(23),
    CMYKCyan(24),
    CMYKMagenta(25),
    CMYKYellow(26),
    CMYKKey(27)
}

data class FillHoles(
    val morphRadius: Int = 11,
    val edgeRadius: Int = 21,
    val closeRadius: Int = 5,
    val channelS: FillHolesChannelS = FillHolesChannelS.All,
    val fillLightColours: Boolean = false,
    val fast: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fill_holes",
        morphRadius.inRange("morphRadius", 3..50),
        edgeRadius.inRange("edgeRadius", 0..50),
        closeRadius.inRange("closeRadius", 0..10),
        channelS,
        fillLightColours,
        fast
    )
)

data class RemoveScratches(
    val threshold: Float = 72f,
    val erosion: Int = 2,
    val dilation: Int = 4
) : RawGmicFilter(
    gmicCommand(
        "fx_remove_scratches",
        threshold.inRange("threshold", 0f, 100f),
        erosion.inRange("erosion", 0..5),
        dilation.inRange("dilation", 0..7)
    )
)

data class RemoveHotPixels(
    val maskSize: Int = 3,
    val threshold: Float = 10f
) : RawGmicFilter(
    gmicCommand(
        "remove_hotpixels",
        maskSize.inRange("maskSize", 3..20),
        threshold.inRange("threshold", 0f, 200f)
    )
)

data class TextureSharpen(
    val strength: Float = 1f,
    val radius: Float = 4f,
    val channelS: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_sharpen_texture",
        strength.inRange("strength", 0f, 4f),
        radius.inRange("radius", 0f, 32f),
        channelS
    )
)
