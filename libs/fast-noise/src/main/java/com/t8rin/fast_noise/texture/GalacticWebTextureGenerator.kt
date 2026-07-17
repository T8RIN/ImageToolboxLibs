package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GalacticWebTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val nodeDensity: Float = 0.72f,
    val gravityWarp: Float = 0.68f,
    val filamentSharpness: Float = 0.80f,
    val glow: Float = 0.86f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF010208.toInt(),
    val primaryColor: Int = 0xFF182B69.toInt(),
    val secondaryColor: Int = 0xFF6B3EA0.toInt(),
    val highlightColor: Int = 0xFFFFE9CA.toInt()
) : ProceduralTextureParameters

class GalacticWebTextureGenerator :
    NativeProceduralTextureGenerator<GalacticWebTextureParameters>(TextureType.GALACTIC_WEB) {
    override fun colors(parameters: GalacticWebTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GalacticWebTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.nodeDensity,
        parameters.gravityWarp,
        parameters.filamentSharpness,
        parameters.glow,
        parameters.contrast
    )
}
