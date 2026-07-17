package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SnakeSkinTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val scaleDensity: Float = 0.62f,
    val irregularity: Float = 0.40f,
    val rimSharpness: Float = 0.72f,
    val sheen: Float = 0.40f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF17170E.toInt(),
    val primaryColor: Int = 0xFF4E5A2D.toInt(),
    val secondaryColor: Int = 0xFF9DA75A.toInt(),
    val highlightColor: Int = 0xFFD9D19A.toInt()
) : ProceduralTextureParameters

class SnakeSkinTextureGenerator :
    NativeProceduralTextureGenerator<SnakeSkinTextureParameters>(TextureType.SNAKE_SKIN) {
    override fun colors(parameters: SnakeSkinTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SnakeSkinTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.scaleDensity,
        parameters.irregularity,
        parameters.rimSharpness,
        parameters.sheen,
        parameters.contrast
    )
}
