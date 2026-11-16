package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SpiralPearlsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val spiralArms: Float = 0.55f,
    val growth: Float = 0.48f,
    val beadSize: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF101C31.toInt(),
    val primaryColor: Int = 0xFF41879E.toInt(),
    val secondaryColor: Int = 0xFFC279A0.toInt(),
    val highlightColor: Int = 0xFFF3DBAD.toInt()
) : ProceduralTextureParameters

class SpiralPearlsTextureGenerator :
    NativeProceduralTextureGenerator<SpiralPearlsTextureParameters>(TextureType.SPIRAL_PEARLS) {
    override fun colors(parameters: SpiralPearlsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SpiralPearlsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.spiralArms,
        parameters.growth,
        parameters.beadSize,
        parameters.polish,
        parameters.contrast
    )
}
