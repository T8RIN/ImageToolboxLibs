package com.t8rin.gmic.filters

data class LightPatch(
    val density: Int = 5,
    val darkness: Float = 0.7f,
    val lightness: Float = 2.5f,
    val channel: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_light_patch",
        density.inRange("density", 2..30),
        darkness.inRange("darkness", 0f, 1f),
        lightness.inRange("lightness", 1f, 4f),
        channel
    )
)

data class ReliefLight(
    val ambientLightness: Float = 0.3f,
    val specularLightness: Float = 0.2f,
    val specularSize: Float = 0.2f,
    val darkness: Float = 0f,
    val lightSmoothness: Float = 1f,
    val lightX: Float = 50f,
    val lightY: Float = 50f,
    val lightZ: Float = 5f,
    val zScale: Float = 0.5f,
    val opacityAsHeightmap: Boolean = false,
    val imageSmoothness: Float = 0f
) : RawGmicFilter(
    gmicCommand(
        "fx_light_relief",
        ambientLightness.inRange("ambientLightness", 0f, 5f),
        specularLightness.inRange("specularLightness", 0f, 2f),
        specularSize.inRange("specularSize", 0f, 1f),
        darkness.inRange("darkness", 0f, 1f),
        lightSmoothness.inRange("lightSmoothness", 0f, 5f),
        lightX.inRange("lightX", 0f, 100f),
        lightY.inRange("lightY", 0f, 100f),
        lightZ.inRange("lightZ", 0f, 20f),
        zScale.inRange("zScale", 0f, 3f),
        opacityAsHeightmap,
        imageSmoothness.inRange("imageSmoothness", 0f, 10f)
    )
)

data class ShadowPatch(
    val opacity: Float = 0.7f,
    val channel: GmicChannel = GmicChannel.All
) : RawGmicFilter(
    gmicCommand(
        "fx_shadow_patch",
        opacity.inRange("opacity", 0f, 1f),
        channel
    )
)
