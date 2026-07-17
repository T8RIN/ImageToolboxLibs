package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BiomechanicalTissueTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val ribDensity: Float = 0.66f,
    val tension: Float = 0.58f,
    val ridgeSharpness: Float = 0.78f,
    val wetSheen: Float = 0.62f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF100C12.toInt(),
    val primaryColor: Int = 0xFF553444.toInt(),
    val secondaryColor: Int = 0xFFB97472.toInt(),
    val highlightColor: Int = 0xFFF0CAB0.toInt()
) : ProceduralTextureParameters

class BiomechanicalTissueTextureGenerator :
    NativeProceduralTextureGenerator<BiomechanicalTissueTextureParameters>(TextureType.BIOMECHANICAL_TISSUE) {
    override fun colors(parameters: BiomechanicalTissueTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BiomechanicalTissueTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ribDensity,
        parameters.tension,
        parameters.ridgeSharpness,
        parameters.wetSheen,
        parameters.contrast
    )
}
