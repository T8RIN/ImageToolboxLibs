package com.t8rin.gmic.filters

enum class SelectiveDesaturationMode(override val value: Int) : GmicArgument {
    ReferenceColor(0),
    AllButReferenceColor(1)
}

enum class SelectiveDesaturationMaximumSaturation(override val value: Int) : GmicArgument {
    FromInput(0),
    FromReferenceColor(1),
    MaximumValue(2)
}

data class SelectiveDesaturation(
    val referenceColor: Int = 0xFFE53935.toInt(),
    val mode: SelectiveDesaturationMode = SelectiveDesaturationMode.AllButReferenceColor,
    val strength: Float = 5f,
    val regularization: Int = 4,
    val maximumSaturation: SelectiveDesaturationMaximumSaturation =
        SelectiveDesaturationMaximumSaturation.FromInput
) : RawGmicFilter(
    gmicCommand(
        "fx_selective_desaturation",
        referenceColor.gmicRed(),
        referenceColor.gmicGreen(),
        referenceColor.gmicBlue(),
        mode,
        strength.inRange("strength", 0f, 10f),
        regularization.inRange("regularization", 0..20),
        maximumSaturation
    )
)

enum class GraduatedColorAccentMaximumValue(override val value: Int) : GmicArgument {
    FromInput(0),
    FromReferenceColor(1),
    MaximumAllowed(2)
}

enum class GraduatedColorAccentChannel(override val value: Int) : GmicArgument {
    Saturation(0),
    Lightness(1)
}

data class GraduatedColorAccent(
    val referenceColor: Int = 0xFFFF9800.toInt(),
    val amplitude: Float = 2.5f,
    val maximumValue: GraduatedColorAccentMaximumValue = GraduatedColorAccentMaximumValue.FromInput,
    val channel: GraduatedColorAccentChannel = GraduatedColorAccentChannel.Saturation
) : RawGmicFilter(
    gmicCommand(
        "fx_gca",
        referenceColor.gmicRed(),
        referenceColor.gmicGreen(),
        referenceColor.gmicBlue(),
        amplitude.inRange("amplitude", 0f, 5f),
        maximumValue,
        channel
    )
)

data class HslAdjustment(
    val contrast: Float = 1.1f,
    val level: Float = 0.05f,
    val autoReduceLevel: Boolean = false,
    val hue: Float = 35f,
    val amount: Float = 0.35f,
    val autoSetInverseHue: Boolean = false,
    val gamma: Float = 1f,
    val outputContrast: Float = 1.05f
) : RawGmicFilter(
    gmicCommand(
        "gcd_hsl",
        contrast.inRange("contrast", 0f, 2f),
        level.inRange("level", -1f, 1f),
        autoReduceLevel,
        hue.inRange("hue", 0f, 360f),
        amount.inRange("amount", 0.2f, 1f),
        autoSetInverseHue,
        gamma.inRange("gamma", 0f, 2f),
        outputContrast.inRange("outputContrast", 0f, 2f)
    )
)

data class SpecificSaturation(
    val input: Float = 1.2f,
    val opposing: Float = 0.9f,
    val hue: Int = 35,
    val level: Float = 0.08f,
    val gamma: Float = 1f,
    val contrast: Float = 1.1f,
    val srgbConversion: Boolean = true,
    val processTopLayerOnly: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "gcd_hio_levels",
        input.inRange("input", 0f, 2f),
        opposing.inRange("opposing", 0f, 2f),
        hue.inRange("hue", 0..360),
        level.inRange("level", -0.5f, 0.5f),
        gamma.inRange("gamma", 0.2f, 1.8f),
        contrast.inRange("contrast", 0.2f, 1.8f),
        srgbConversion,
        processTopLayerOnly
    )
)

enum class ChannelProcessingValueAction(override val value: Int) : GmicArgument {
    NoneOption(0),
    Cut(1),
    CutAndNormalize(2),
    Normalize(3),
    Threshold(4)
}

enum class ChannelProcessingTonesRange(override val value: Int) : GmicArgument {
    AllTones(0),
    Shadows(1),
    Midtones(2),
    Highlights(3)
}

data class ChannelProcessing(
    val brightness: Float = 5f,
    val contrast: Float = 12f,
    val gamma: Float = 0f,
    val smoothness: Float = 0f,
    val valueAction: ChannelProcessingValueAction = ChannelProcessingValueAction.NoneOption,
    val lowValue: Float = 0f,
    val highValue: Float = 100f,
    val quantization: Int = 256,
    val equalization: Boolean = false,
    val negation: Boolean = false,
    val tonesRange: ChannelProcessingTonesRange = ChannelProcessingTonesRange.Midtones,
    val tonesSmoothness: Float = 2f,
    val channelS: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_channel_processing",
        brightness.inRange("brightness", -100f, 100f),
        contrast.inRange("contrast", -100f, 100f),
        gamma.inRange("gamma", -100f, 100f),
        smoothness.inRange("smoothness", 0f, 10f),
        valueAction,
        lowValue.inRange("lowValue", 0f, 100f),
        highValue.inRange("highValue", 0f, 100f),
        quantization.inRange("quantization", 1..256),
        equalization,
        negation,
        tonesRange,
        tonesSmoothness.inRange("tonesSmoothness", 0f, 10f),
        channelS
    )
)

data class HsvEqualizer(
    val firstHueBand: Float = 25f,
    val firstBandWidth: Float = 55f,
    val firstHueShift: Float = 8f,
    val firstSaturationCorrection: Float = 0.2f,
    val firstValueCorrection: Float = 0.04f,
    val secondHueBand: Float = 200f,
    val secondBandWidth: Float = 70f,
    val secondHueShift: Float = -6f,
    val secondSaturationCorrection: Float = 0.14f,
    val secondValueCorrection: Float = -0.03f,
    val thirdHueBand: Float = 330f,
    val thirdBandWidth: Float = 45f,
    val thirdHueShift: Float = 4f,
    val thirdSaturationCorrection: Float = 0.1f,
    val thirdValueCorrection: Float = 0.06f
) : RawGmicFilter(
    gmicCommand(
        "fx_hsv_equalizer",
        false,
        firstHueBand.inRange("firstHueBand", 0f, 360f),
        firstBandWidth.inRange("firstBandWidth", 1f, 360f),
        firstHueShift.inRange("firstHueShift", -180f, 180f),
        firstSaturationCorrection.inRange("firstSaturationCorrection", -0.99f, 0.99f),
        firstValueCorrection.inRange("firstValueCorrection", -0.99f, 0.99f),
        secondHueBand.inRange("secondHueBand", 0f, 360f),
        secondBandWidth.inRange("secondBandWidth", 1f, 360f),
        secondHueShift.inRange("secondHueShift", -180f, 180f),
        secondSaturationCorrection.inRange("secondSaturationCorrection", -0.99f, 0.99f),
        secondValueCorrection.inRange("secondValueCorrection", -0.99f, 0.99f),
        thirdHueBand.inRange("thirdHueBand", 0f, 360f),
        thirdBandWidth.inRange("thirdBandWidth", 1f, 360f),
        thirdHueShift.inRange("thirdHueShift", -180f, 180f),
        thirdSaturationCorrection.inRange("thirdSaturationCorrection", -0.99f, 0.99f),
        thirdValueCorrection.inRange("thirdValueCorrection", -0.99f, 0.99f)
    )
)

enum class TargetColorSpotChromaticityRange(override val value: Int) : GmicArgument {
    Global(0),
    Wide(1),
    Narrow(2)
}

data class TargetColorSpot(
    val xPercent: Float = 50f,
    val yPercent: Float = 50f,
    val targetColor: Int = 0xFFFFA726.toInt(),
    val chromaticityRange: TargetColorSpotChromaticityRange = TargetColorSpotChromaticityRange.Wide
) : RawGmicFilter(
    gmicCommand(
        "fx_gcd_color_target",
        xPercent.inRange("xPercent", 0f, 100f),
        yPercent.inRange("yPercent", 0f, 100f),
        targetColor.gmicRed(),
        targetColor.gmicGreen(),
        targetColor.gmicBlue(),
        chromaticityRange
    )
)

enum class BlackAndWhiteFilmType(override val value: Int) : GmicArgument {
    Manual(0),
    Agfa200X(1),
    Agfapan25(2),
    Agfapan100(3),
    Agfapan400(4),
    IlfordDelta100(5),
    IlfordDelta400(6),
    IlfordDelta3200(7),
    IlfordFp4(8),
    IlfordHp4(9),
    IlfordPanF(10),
    IlfordSfx(11),
    IlfordXp2Super(12),
    KodakTmax100(13),
    KodakTmax400(14),
    KodakTriX(15)
}

enum class BlackAndWhiteFilmGrainType(override val value: Int) : GmicArgument {
    Gaussian(0),
    Uniform(1),
    SaltAndPepper(2),
    Poisson(3)
}

data class BlackAndWhiteFilm(
    val filmType: BlackAndWhiteFilmType = BlackAndWhiteFilmType.KodakTriX,
    val redLevel: Float = 0.299f,
    val redSmoothness: Float = 0f,
    val greenLevel: Float = 0.587f,
    val greenSmoothness: Float = 0f,
    val blueLevel: Float = 0.114f,
    val blueSmoothness: Float = 0f,
    val gamma: Float = 1.05f,
    val contrast: Float = 1.2f,
    val brightness: Float = 0f,
    val hue: Float = 30f,
    val saturation: Float = 0.08f,
    val grainShadows: Float = 18f,
    val grainMidtones: Float = 10f,
    val grainHighlights: Float = 5f,
    val grainToneFading: Float = 2f,
    val grainScale: Float = 0.8f,
    val grainType: BlackAndWhiteFilmGrainType = BlackAndWhiteFilmGrainType.Gaussian,
    val localContrast: Float = 8f,
    val radius: Int = 16,
    val contrastSmoothness: Float = 4f
) : RawGmicFilter(
    gmicCommand(
        "fx_bwfilmsimulate",
        filmType,
        redLevel.inRange("redLevel", 0f, 1f),
        redSmoothness.inRange("redSmoothness", 0f, 10f),
        greenLevel.inRange("greenLevel", 0f, 1f),
        greenSmoothness.inRange("greenSmoothness", 0f, 10f),
        blueLevel.inRange("blueLevel", 0f, 1f),
        blueSmoothness.inRange("blueSmoothness", 0f, 10f),
        gamma.inRange("gamma", 0.01f, 5f),
        contrast.inRange("contrast", 0f, 4f),
        brightness.inRange("brightness", -255f, 255f),
        hue.inRange("hue", 0f, 360f),
        saturation.inRange("saturation", 0f, 1f),
        grainShadows.inRange("grainShadows", 0f, 200f),
        grainMidtones.inRange("grainMidtones", 0f, 200f),
        grainHighlights.inRange("grainHighlights", 0f, 200f),
        grainToneFading.inRange("grainToneFading", 0f, 10f),
        grainScale.inRange("grainScale", 0f, 3f),
        grainType,
        localContrast.inRange("localContrast", 0f, 60f),
        radius.inRange("radius", 1..512),
        contrastSmoothness.inRange("contrastSmoothness", 0f, 10f)
    )
)

data class LocalContrastEnhancement(
    val spatialRadius: Float = 80f,
    val amount: Float = 1.2f,
    val darknessLevel: Float = 1.2f,
    val lightnessLevel: Float = 1.2f,
    val channelS: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_LCE",
        spatialRadius.inRange("spatialRadius", 30f, 200f),
        amount.inRange("amount", 0f, 5f),
        darknessLevel.inRange("darknessLevel", 0f, 4f),
        lightnessLevel.inRange("lightnessLevel", 0f, 4f),
        channelS
    )
)

data class LocalVarianceNormalization(
    val amplitude: Float = 60f,
    val smoothness: Float = 6f,
    val threshold: Float = 10f,
    val repeat: Int = 1,
    val channelS: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "jeje_normalize_local_variance",
        amplitude.inRange("amplitude", 0f, 100f),
        smoothness.inRange("smoothness", 0f, 20f),
        threshold.inRange("threshold", 0f, 100f),
        repeat.inRange("repeat", 1..4),
        channelS
    )
)

enum class SharpenValueAction(override val value: Int) : GmicArgument {
    Cut(0),
    NormalizeLuma(1)
}

data class GradientSharpen(
    val amount: Float = 0.8f,
    val scale: Float = 1.4f,
    val valueAction: SharpenValueAction = SharpenValueAction.NormalizeLuma
) : RawGmicFilter(
    gmicCommand(
        "gcd_sharpen_gradient",
        amount.inRange("amount", 0f, 2f),
        scale.inRange("scale", 0.1f, 2f),
        valueAction
    )
)

data class ToneSharpen(
    val amount: Float = 1.2f,
    val center: Int = 128,
    val valueAction: SharpenValueAction = SharpenValueAction.NormalizeLuma
) : RawGmicFilter(
    gmicCommand(
        "gcd_sharpen_tones",
        amount.inRange("amount", 0f, 4f),
        center.inRange("center", 0..255),
        valueAction
    )
)

enum class ConstrainedSharpenChannel(override val value: Int) : GmicArgument {
    All(0),
    Rgba(1),
    Rgb(2),
    Red(3),
    Green(4),
    Blue(5),
    Alpha(6),
    LinearRgb(7),
    LinearRed(8),
    LinearGreen(9),
    LinearBlue(10),
    Luminance(11),
    Chrominance(12),
    BlueChrominance(13),
    RedChrominance(14),
    GreenChrominance(15),
    LabLightness(16),
    LabChrominance(17),
    LabA(18),
    LabB(19),
    LchChrominance(20),
    LchChroma(21),
    LchHue(22),
    HsvHue(23),
    HsvSaturation(24),
    HsvValue(25),
    HsiIntensity(26),
    HslLightness(27),
    Cyan(28),
    Magenta(29),
    Yellow(30),
    Key(31)
}

enum class ConstrainedSharpenValueAction(override val value: Int) : GmicArgument {
    NoneOption(0),
    Cut(1),
    Normalize(2)
}

data class ConstrainedSharpen(
    val sharpenRadius: Float = 0.75f,
    val amount: Float = 2.5f,
    val threshold: Float = 1f,
    val constraintRadius: Int = 5,
    val overshoot: Float = 0f,
    val channelS: ConstrainedSharpenChannel = ConstrainedSharpenChannel.Luminance,
    val valueAction: ConstrainedSharpenValueAction = ConstrainedSharpenValueAction.Cut
) : RawGmicFilter(
    gmicCommand(
        "iain_constrained_sharpen",
        sharpenRadius.inRange("sharpenRadius", 0f, 10f),
        amount.inRange("amount", 0f, 10f),
        threshold.inRange("threshold", 0f, 50f),
        constraintRadius.inRange("constraintRadius", 0..10),
        overshoot.inRange("overshoot", 0f, 50f),
        channelS,
        valueAction
    )
)

data class HighlightSynthesis(
    val red: Float = 1.1f,
    val green: Float = 0.7f,
    val blue: Float = 1.15f,
    val rgbToSrgb: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "iain_highlight_synthesis",
        red.inRange("red", 0f, 2f),
        green.inRange("green", 0f, 2f),
        blue.inRange("blue", 0f, 2f),
        rgbToSrgb
    )
)

data class DesaturateNorm(
    val red: Float = 0.6f,
    val blue: Float = 0.4f,
    val exponent: Float = 2.2f
) : RawGmicFilter(
    gmicCommand(
        "fx_gcd_norm_eq",
        red.inRange("red", 0f, 1f),
        blue.inRange("blue", 0f, 1f),
        exponent.inRange("exponent", 1f, 3f)
    )
)

data class SaturationEqualizer(
    val black: Float = -12f,
    val nearBlack: Float = -6f,
    val darkGray: Float = 4f,
    val midDarkGray: Float = 8f,
    val middleGray: Float = 12f,
    val midLightGray: Float = 8f,
    val lightGray: Float = 4f,
    val highlights: Float = -2f,
    val white: Float = -8f,
    val hue0: Float = 8f,
    val hue45: Float = 14f,
    val hue90: Float = 6f,
    val hue135: Float = -4f,
    val hue180: Float = -8f,
    val hue225: Float = 4f,
    val hue270: Float = 10f,
    val hue315: Float = 12f,
    val hue360: Float = 8f,
    val rotateHueBands: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "Saturation_EQ_p",
        black.inRange("black", -128f, 128f),
        nearBlack.inRange("nearBlack", -128f, 128f),
        darkGray.inRange("darkGray", -128f, 128f),
        midDarkGray.inRange("midDarkGray", -128f, 128f),
        middleGray.inRange("middleGray", -128f, 128f),
        midLightGray.inRange("midLightGray", -128f, 128f),
        lightGray.inRange("lightGray", -128f, 128f),
        highlights.inRange("highlights", -128f, 128f),
        white.inRange("white", -128f, 128f),
        hue0.inRange("hue0", -128f, 128f),
        hue45.inRange("hue45", -128f, 128f),
        hue90.inRange("hue90", -128f, 128f),
        hue135.inRange("hue135", -128f, 128f),
        hue180.inRange("hue180", -128f, 128f),
        hue225.inRange("hue225", -128f, 128f),
        hue270.inRange("hue270", -128f, 128f),
        hue315.inRange("hue315", -128f, 128f),
        hue360.inRange("hue360", -128f, 128f),
        rotateHueBands.inRange("rotateHueBands", -45f, 45f)
    )
)

data class DynamicContrast(
    val amount: Float = 12f,
    val threshold: Float = 128f,
    val intensity: Float = 220f
) : RawGmicFilter(
    gmicCommand(
        "rep_dynamic_contrast",
        amount.inRange("amount", -64f, 64f),
        threshold.inRange("threshold", 0f, 255f),
        intensity.inRange("intensity", 0f, 255f)
    )
)

enum class ToneEnhanceChannel(override val value: Int) : GmicArgument {
    Hsi(0),
    Hsv(1),
    Lab(2),
    LinearRgb(3),
    Rgb(4),
    YCbCr(5)
}

enum class ToneEnhanceValues(override val value: Int) : GmicArgument {
    Cut(0),
    Normalize(1)
}

data class ToneEnhance(
    val shadowsDetail: Float = 0.6f,
    val shadowsGamma: Float = 0.9f,
    val highlightsDetail: Float = 0.5f,
    val highlightsGamma: Float = 1.1f,
    val center: Int = 128,
    val midpointDetail: Float = 0.4f,
    val midpointGamma: Float = 1f,
    val recoveryBoost: Float = 0.6f,
    val recoverySmoothness: Float = 0.2f,
    val detailMaskSmoothness: Int = 3,
    val channel: ToneEnhanceChannel = ToneEnhanceChannel.YCbCr,
    val values: ToneEnhanceValues = ToneEnhanceValues.Cut,
    val colorMedian: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "gcd_tone_enhance",
        shadowsDetail.inRange("shadowsDetail", 0f, 2f),
        shadowsGamma.inRange("shadowsGamma", 0.2f, 1.8f),
        highlightsDetail.inRange("highlightsDetail", 0f, 2f),
        highlightsGamma.inRange("highlightsGamma", 0.2f, 1.8f),
        center.inRange("center", 0..255),
        midpointDetail.inRange("midpointDetail", 0f, 2f),
        midpointGamma.inRange("midpointGamma", 0.2f, 1.8f),
        recoveryBoost.inRange("recoveryBoost", 0f, 1f),
        recoverySmoothness.inRange("recoverySmoothness", 0f, 2f),
        detailMaskSmoothness.inRange("detailMaskSmoothness", 0..20),
        channel,
        values,
        colorMedian
    )
)
