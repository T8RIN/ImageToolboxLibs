package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MercuryDropletsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val dropDensity: Float = 0.55f,
    val sizeVariation: Float = 0.48f,
    val roundness: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0C2536.toInt(),
    val primaryColor: Int = 0xFF587887.toInt(),
    val secondaryColor: Int = 0xFFCFAE7E.toInt(),
    val highlightColor: Int = 0xFFF0F0D5.toInt()
) : ProceduralTextureParameters

class MercuryDropletsTextureGenerator :
    NativeProceduralTextureGenerator<MercuryDropletsTextureParameters>(TextureType.MERCURY_DROPLETS) {
    override fun colors(parameters: MercuryDropletsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MercuryDropletsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.dropDensity,
        parameters.sizeVariation,
        parameters.roundness,
        parameters.polish,
        parameters.contrast
    )
}
