package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SeigaihaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val rotation: Float = 0.5f,
    val lineWidth: Float = 0.3f,
    val ringCount: Float = 0.5f,
    val variation: Float = 0.55f,
    val roughness: Float = 0.3f,
    val backgroundColor: Int = 0xFF092340.toInt(),
    val primaryColor: Int = 0xFF1A658B.toInt(),
    val secondaryColor: Int = 0xFF42A6BA.toInt(),
    val highlightColor: Int = 0xFFE6F3DA.toInt()
) : ProceduralTextureParameters

class SeigaihaTextureGenerator :
    NativeProceduralTextureGenerator<SeigaihaTextureParameters>(TextureType.SEIGAIHA) {
    override fun colors(parameters: SeigaihaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SeigaihaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.lineWidth,
        parameters.ringCount,
        parameters.variation,
        parameters.roughness
    )
}
