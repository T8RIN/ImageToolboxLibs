package com.t8rin.gmic.filters

enum class BoostChromaticityColorSpace(override val value: Int) : GmicArgument {
    YCbCrDistinct(0),
    YCbCrMixed(1),
    LabDistinct(2),
    LabMixed(3)
}

data class BoostChromaticity(
    val amplitudePercent: Float = 75f,
    val colorSpace: BoostChromaticityColorSpace = BoostChromaticityColorSpace.LabMixed
) : RawGmicFilter(
    gmicCommand(
        "fx_boost_chroma",
        amplitudePercent.inRange("amplitudePercent", 0f, 100f),
        colorSpace
    )
)

enum class EqualizeLightMode(override val value: Int) : GmicArgument {
    PreserveRange(0),
    PreserveCovariance(1)
}

data class EqualizeLight(
    val amountPercent: Float = 88f,
    val mode: EqualizeLightMode = EqualizeLightMode.PreserveCovariance,
    val channelS: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_equalize_light",
        amountPercent.inRange("amountPercent", 0f, 100f),
        mode,
        channelS
    )
)

data class EqualizeShadow(
    val amplitude: Float = 0.8f
) : RawGmicFilter(
    gmicCommand(
        "fx_equalize_shadow",
        amplitude.inRange("amplitude", 0f, 1f)
    )
)

enum class LightGlowMode(override val value: Int) : GmicArgument {
    Burn(0),
    Dodge(1),
    Freeze(2),
    GrainMerge(3),
    HardLight(4),
    Interpolation(5),
    Lighten(6),
    Multiply(7),
    Overlay(8),
    Reflect(9),
    SoftLight(10),
    Stamp(11),
    Value(12)
}

data class LightGlow(
    val density: Float = 30f,
    val amplitude: Float = 0.5f,
    val mode: LightGlowMode = LightGlowMode.Overlay,
    val opacity: Float = 0.8f,
    val channelS: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_lightglow",
        density.inRange("density", 0f, 100f),
        amplitude.inRange("amplitude", 0f, 2f),
        mode,
        opacity.inRange("opacity", 0f, 1f),
        channelS
    )
)

data class ColorTemperature(
    val colorTemperature: Float = 8f,
    val automaticColorBalance: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_tk_colortemp",
        colorTemperature.inRange("colorTemperature", -20f, 20f),
        automaticColorBalance
    )
)

enum class TemperatureBalanceLighting(override val value: Int) : GmicArgument {
    Automatic(0),
    AutomaticScanAllHues(1),
    StandardNoScan(2)
}

data class TemperatureBalance(
    val saturation: Float = 0.2f,
    val level: Float = 0.1f,
    val color: Float = 1.15f,
    val lighting: TemperatureBalanceLighting = TemperatureBalanceLighting.AutomaticScanAllHues
) : RawGmicFilter(
    gmicCommand(
        "gcd_temp_balance",
        saturation.inRange("saturation", -1f, 1f),
        level.inRange("level", -1f, 1f),
        color.inRange("color", 0f, 2f),
        lighting
    )
)
