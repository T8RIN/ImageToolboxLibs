package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class IridescentFoamTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bubbleDensity: Float = 0.55f,
    val sizeVariation: Float = 0.48f,
    val filmWidth: Float = 0.55f,
    val iridescence: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0B263B.toInt(),
    val primaryColor: Int = 0xFF4C869E.toInt(),
    val secondaryColor: Int = 0xFFC08DB5.toInt(),
    val highlightColor: Int = 0xFFECE4BD.toInt()
) : ProceduralTextureParameters

class IridescentFoamTextureGenerator :
    NativeProceduralTextureGenerator<IridescentFoamTextureParameters>(TextureType.IRIDESCENT_FOAM) {
    override fun colors(parameters: IridescentFoamTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: IridescentFoamTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bubbleDensity,
        parameters.sizeVariation,
        parameters.filmWidth,
        parameters.iridescence,
        parameters.contrast
    )
}
