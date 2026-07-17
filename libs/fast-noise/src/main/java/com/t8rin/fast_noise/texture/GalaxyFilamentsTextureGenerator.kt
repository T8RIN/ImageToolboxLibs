package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GalaxyFilamentsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val filamentDensity: Float = 0.70f,
    val turbulence: Float = 0.62f,
    val filamentSharpness: Float = 0.78f,
    val starGlow: Float = 0.84f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF02030A.toInt(),
    val primaryColor: Int = 0xFF173B78.toInt(),
    val secondaryColor: Int = 0xFF8137A6.toInt(),
    val highlightColor: Int = 0xFFFFE7BE.toInt()
) : ProceduralTextureParameters

class GalaxyFilamentsTextureGenerator :
    NativeProceduralTextureGenerator<GalaxyFilamentsTextureParameters>(TextureType.GALAXY_FILAMENTS) {
    override fun colors(parameters: GalaxyFilamentsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GalaxyFilamentsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.filamentDensity,
        parameters.turbulence,
        parameters.filamentSharpness,
        parameters.starGlow,
        parameters.contrast
    )
}
