package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PlasmaTendrilTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val arcCount: Float = 0.72f,
    val turbulence: Float = 0.72f,
    val arcSharpness: Float = 0.82f,
    val glow: Float = 0.9f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF030511.toInt(),
    val primaryColor: Int = 0xFF173A91.toInt(),
    val secondaryColor: Int = 0xFF32C8FF.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class PlasmaTendrilTextureGenerator :
    NativeProceduralTextureGenerator<PlasmaTendrilTextureParameters>(TextureType.PLASMA_TENDRIL) {
    override fun colors(parameters: PlasmaTendrilTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PlasmaTendrilTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.arcCount,
        parameters.turbulence,
        parameters.arcSharpness,
        parameters.glow,
        parameters.contrast
    )
}
