package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LavaCrustTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val plateDensity: Float = 0.55f,
    val irregularity: Float = 0.48f,
    val crustDepth: Float = 0.55f,
    val heat: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF141B24.toInt(),
    val primaryColor: Int = 0xFF493C42.toInt(),
    val secondaryColor: Int = 0xFFDB692D.toInt(),
    val highlightColor: Int = 0xFFFFE0A0.toInt()
) : ProceduralTextureParameters

class LavaCrustTextureGenerator :
    NativeProceduralTextureGenerator<LavaCrustTextureParameters>(TextureType.LAVA_CRUST) {
    override fun colors(parameters: LavaCrustTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LavaCrustTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.plateDensity,
        parameters.irregularity,
        parameters.crustDepth,
        parameters.heat,
        parameters.contrast
    )
}
