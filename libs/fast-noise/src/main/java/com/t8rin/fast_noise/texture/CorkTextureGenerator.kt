package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CorkTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val chipDensity: Float = 0.55f,
    val irregularity: Float = 0.48f,
    val pores: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF322114.toInt(),
    val primaryColor: Int = 0xFF967448.toInt(),
    val secondaryColor: Int = 0xFFC6A274.toInt(),
    val highlightColor: Int = 0xFFEDD4AA.toInt()
) : ProceduralTextureParameters

class CorkTextureGenerator :
    NativeProceduralTextureGenerator<CorkTextureParameters>(TextureType.CORK) {
    override fun colors(parameters: CorkTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CorkTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.chipDensity,
        parameters.irregularity,
        parameters.pores,
        parameters.relief,
        parameters.contrast
    )
}
