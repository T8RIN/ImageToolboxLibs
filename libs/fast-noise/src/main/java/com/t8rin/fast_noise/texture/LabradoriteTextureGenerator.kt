package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LabradoriteTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val facetDensity: Float = 0.55f,
    val cleavage: Float = 0.48f,
    val relief: Float = 0.55f,
    val flash: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF101B27.toInt(),
    val primaryColor: Int = 0xFF273E54.toInt(),
    val secondaryColor: Int = 0xFF338BBA.toInt(),
    val highlightColor: Int = 0xFFEDD188.toInt()
) : ProceduralTextureParameters

class LabradoriteTextureGenerator :
    NativeProceduralTextureGenerator<LabradoriteTextureParameters>(TextureType.LABRADORITE) {
    override fun colors(parameters: LabradoriteTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LabradoriteTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.facetDensity,
        parameters.cleavage,
        parameters.relief,
        parameters.flash,
        parameters.contrast
    )
}
