package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RayleighBenardTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val cellDensity: Float = 0.70f,
    val convection: Float = 0.62f,
    val edgeSharpness: Float = 0.72f,
    val heatGlow: Float = 0.82f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF180603.toInt(),
    val primaryColor: Int = 0xFF862208.toInt(),
    val secondaryColor: Int = 0xFFE76A16.toInt(),
    val highlightColor: Int = 0xFFFFEDA0.toInt()
) : ProceduralTextureParameters

class RayleighBenardTextureGenerator :
    NativeProceduralTextureGenerator<RayleighBenardTextureParameters>(TextureType.RAYLEIGH_BENARD) {
    override fun colors(parameters: RayleighBenardTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RayleighBenardTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.cellDensity,
        parameters.convection,
        parameters.edgeSharpness,
        parameters.heatGlow,
        parameters.contrast
    )
}
