package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class DragonScalesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val scaleDensity: Float = 0.58f,
    val curvature: Float = 0.35f,
    val rimSharpness: Float = 0.72f,
    val iridescence: Float = 0.60f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF071913.toInt(),
    val primaryColor: Int = 0xFF17664B.toInt(),
    val secondaryColor: Int = 0xFF39B778.toInt(),
    val highlightColor: Int = 0xFFE0C96B.toInt()
) : ProceduralTextureParameters

class DragonScalesTextureGenerator :
    NativeProceduralTextureGenerator<DragonScalesTextureParameters>(TextureType.DRAGON_SCALES) {
    override fun colors(parameters: DragonScalesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: DragonScalesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.scaleDensity,
        parameters.curvature,
        parameters.rimSharpness,
        parameters.iridescence,
        parameters.contrast
    )
}
