package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class NeonCityTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val buildingDensity: Float = 0.68f,
    val perspective: Float = 0.52f,
    val edgeSharpness: Float = 0.80f,
    val neonGlow: Float = 0.86f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF02040C.toInt(),
    val primaryColor: Int = 0xFF142D59.toInt(),
    val secondaryColor: Int = 0xFFE638A1.toInt(),
    val highlightColor: Int = 0xFF57F5E9.toInt()
) : ProceduralTextureParameters

class NeonCityTextureGenerator :
    NativeProceduralTextureGenerator<NeonCityTextureParameters>(TextureType.NEON_CITY) {
    override fun colors(parameters: NeonCityTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: NeonCityTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.buildingDensity,
        parameters.perspective,
        parameters.edgeSharpness,
        parameters.neonGlow,
        parameters.contrast
    )
}
