package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HammeredMetalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.028f,
    val irregularity: Float = 0.7f,
    val depth: Float = 0.8f,
    val polish: Float = 0.8f,
    val roughness: Float = 0.25f,
    val patina: Float = 0.3f,
    val backgroundColor: Int = 0xFF28140E.toInt(),
    val primaryColor: Int = 0xFFB66634.toInt(),
    val secondaryColor: Int = 0xFF367E78.toInt(),
    val highlightColor: Int = 0xFFFFE2A6.toInt()
) : ProceduralTextureParameters

class HammeredMetalTextureGenerator :
    NativeProceduralTextureGenerator<HammeredMetalTextureParameters>(TextureType.HAMMERED_METAL) {
    override fun colors(parameters: HammeredMetalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HammeredMetalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.irregularity,
        parameters.depth,
        parameters.polish,
        parameters.roughness,
        parameters.patina
    )
}
