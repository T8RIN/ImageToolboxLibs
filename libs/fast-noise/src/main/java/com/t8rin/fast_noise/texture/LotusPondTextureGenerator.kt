package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LotusPondTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val padDensity: Float = 0.55f,
    val padSize: Float = 0.48f,
    val ripples: Float = 0.55f,
    val blossoms: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF082B36.toInt(),
    val primaryColor: Int = 0xFF337765.toInt(),
    val secondaryColor: Int = 0xFFC18C9D.toInt(),
    val highlightColor: Int = 0xFFE7DDAC.toInt()
) : ProceduralTextureParameters

class LotusPondTextureGenerator :
    NativeProceduralTextureGenerator<LotusPondTextureParameters>(TextureType.LOTUS_POND) {
    override fun colors(parameters: LotusPondTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LotusPondTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.padDensity,
        parameters.padSize,
        parameters.ripples,
        parameters.blossoms,
        parameters.contrast
    )
}
