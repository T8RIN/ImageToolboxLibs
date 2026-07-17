package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PhyllotaxisBloomTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val seedDensity: Float = 0.68f,
    val spiralAngle: Float = 0.62f,
    val seedSharpness: Float = 0.82f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF07100D.toInt(),
    val primaryColor: Int = 0xFF1F6F4A.toInt(),
    val secondaryColor: Int = 0xFFE0A733.toInt(),
    val highlightColor: Int = 0xFFFFF0A0.toInt()
) : ProceduralTextureParameters

class PhyllotaxisBloomTextureGenerator :
    NativeProceduralTextureGenerator<PhyllotaxisBloomTextureParameters>(TextureType.PHYLLOTAXIS_BLOOM) {
    override fun colors(parameters: PhyllotaxisBloomTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PhyllotaxisBloomTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.seedDensity,
        parameters.spiralAngle,
        parameters.seedSharpness,
        parameters.glow,
        parameters.contrast
    )
}
