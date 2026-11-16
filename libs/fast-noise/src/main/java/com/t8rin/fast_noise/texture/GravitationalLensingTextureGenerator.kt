package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GravitationalLensingTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val galaxyDensity: Float = 0.55f,
    val lensStrength: Float = 0.48f,
    val arcWidth: Float = 0.55f,
    val stars: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF081528.toInt(),
    val primaryColor: Int = 0xFF3A7092.toInt(),
    val secondaryColor: Int = 0xFFC7A374.toInt(),
    val highlightColor: Int = 0xFFEDF3D2.toInt()
) : ProceduralTextureParameters

class GravitationalLensingTextureGenerator :
    NativeProceduralTextureGenerator<GravitationalLensingTextureParameters>(TextureType.GRAVITATIONAL_LENSING) {
    override fun colors(parameters: GravitationalLensingTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GravitationalLensingTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.galaxyDensity,
        parameters.lensStrength,
        parameters.arcWidth,
        parameters.stars,
        parameters.contrast
    )
}
