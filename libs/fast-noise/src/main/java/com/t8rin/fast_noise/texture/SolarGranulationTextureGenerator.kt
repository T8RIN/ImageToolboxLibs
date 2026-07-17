package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SolarGranulationTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val granuleDensity: Float = 0.72f,
    val convection: Float = 0.58f,
    val edgeSharpness: Float = 0.74f,
    val heatGlow: Float = 0.86f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF1C0502.toInt(),
    val primaryColor: Int = 0xFF8B2108.toInt(),
    val secondaryColor: Int = 0xFFF06A12.toInt(),
    val highlightColor: Int = 0xFFFFF0A0.toInt()
) : ProceduralTextureParameters

class SolarGranulationTextureGenerator :
    NativeProceduralTextureGenerator<SolarGranulationTextureParameters>(TextureType.SOLAR_GRANULATION) {
    override fun colors(parameters: SolarGranulationTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SolarGranulationTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.granuleDensity,
        parameters.convection,
        parameters.edgeSharpness,
        parameters.heatGlow,
        parameters.contrast
    )
}
