package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TerrazzoTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val chipDensity: Float = 0.58f,
    val variation: Float = 0.32f,
    val chipSharpness: Float = 0.68f,
    val polish: Float = 0.28f,
    val contrast: Float = 1.25f,
    val backgroundColor: Int = 0xFFE5DCCF.toInt(),
    val primaryColor: Int = 0xFF315C72.toInt(),
    val secondaryColor: Int = 0xFFC85554.toInt(),
    val highlightColor: Int = 0xFFE5A73D.toInt()
) : ProceduralTextureParameters

class TerrazzoTextureGenerator :
    NativeProceduralTextureGenerator<TerrazzoTextureParameters>(TextureType.TERRAZZO) {
    override fun colors(parameters: TerrazzoTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TerrazzoTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.chipDensity,
        parameters.variation,
        parameters.chipSharpness,
        parameters.polish,
        parameters.contrast
    )
}
