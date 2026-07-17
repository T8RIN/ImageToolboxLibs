package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LunarEjectaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val craterDensity: Float = 0.64f,
    val rayLength: Float = 0.48f,
    val rimSharpness: Float = 0.80f,
    val albedo: Float = 0.50f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF0B0D11.toInt(),
    val primaryColor: Int = 0xFF353A42.toInt(),
    val secondaryColor: Int = 0xFF8B9098.toInt(),
    val highlightColor: Int = 0xFFE6E1D5.toInt()
) : ProceduralTextureParameters

class LunarEjectaTextureGenerator :
    NativeProceduralTextureGenerator<LunarEjectaTextureParameters>(TextureType.LUNAR_EJECTA) {
    override fun colors(parameters: LunarEjectaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LunarEjectaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.craterDensity,
        parameters.rayLength,
        parameters.rimSharpness,
        parameters.albedo,
        parameters.contrast
    )
}
