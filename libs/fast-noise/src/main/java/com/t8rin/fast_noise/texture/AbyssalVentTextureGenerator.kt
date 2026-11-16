package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class AbyssalVentTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val chimneyCount: Float = 0.55f,
    val current: Float = 0.48f,
    val smokeDensity: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF061D2C.toInt(),
    val primaryColor: Int = 0xFF254D5E.toInt(),
    val secondaryColor: Int = 0xFF9C8070.toInt(),
    val highlightColor: Int = 0xFFC6D6B7.toInt()
) : ProceduralTextureParameters

class AbyssalVentTextureGenerator :
    NativeProceduralTextureGenerator<AbyssalVentTextureParameters>(TextureType.ABYSSAL_VENT) {
    override fun colors(parameters: AbyssalVentTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: AbyssalVentTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.chimneyCount,
        parameters.current,
        parameters.smokeDensity,
        parameters.glow,
        parameters.contrast
    )
}
