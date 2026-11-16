package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CorrugatedMetalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val angle: Float = 0.5f,
    val sharpness: Float = 0.65f,
    val polish: Float = 0.8f,
    val roughness: Float = 0.25f,
    val patina: Float = 0.2f,
    val backgroundColor: Int = 0xFF121D29.toInt(),
    val primaryColor: Int = 0xFF6B8297.toInt(),
    val secondaryColor: Int = 0xFFA76032.toInt(),
    val highlightColor: Int = 0xFFECF6FC.toInt()
) : ProceduralTextureParameters

class CorrugatedMetalTextureGenerator :
    NativeProceduralTextureGenerator<CorrugatedMetalTextureParameters>(TextureType.CORRUGATED_METAL) {
    override fun colors(parameters: CorrugatedMetalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CorrugatedMetalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.angle,
        parameters.sharpness,
        parameters.polish,
        parameters.roughness,
        parameters.patina
    )
}
