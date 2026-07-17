package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FrostFernTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branches: Float = 0.72f,
    val branching: Float = 0.48f,
    val crystalSharpness: Float = 0.70f,
    val frostGlow: Float = 0.78f,
    val contrast: Float = 1.30f,
    val backgroundColor: Int = 0xFF10273A.toInt(),
    val primaryColor: Int = 0xFF559AB4.toInt(),
    val secondaryColor: Int = 0xFFB8E8ED.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class FrostFernTextureGenerator :
    NativeProceduralTextureGenerator<FrostFernTextureParameters>(TextureType.FROST_FERN) {
    override fun colors(parameters: FrostFernTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FrostFernTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branches,
        parameters.branching,
        parameters.crystalSharpness,
        parameters.frostGlow,
        parameters.contrast
    )
}
