package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BrickTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val aspectRatio: Float = 2.15f,
    val mortarWidth: Float = 0.09f,
    val irregularity: Float = 0.18f,
    val roughness: Float = 0.42f,
    val bevel: Float = 0.55f,
    val mortarColor: Int = 0xFFB8AD99.toInt(),
    val darkBrickColor: Int = 0xFF6F2418.toInt(),
    val brickColor: Int = 0xFFB64C32.toInt(),
    val highlightColor: Int = 0xFFD77A55.toInt()
) : ProceduralTextureParameters

class BrickTextureGenerator :
    NativeProceduralTextureGenerator<BrickTextureParameters>(TextureType.BRICKS) {
    override fun colors(parameters: BrickTextureParameters) = intArrayOf(
        parameters.mortarColor,
        parameters.darkBrickColor,
        parameters.brickColor,
        parameters.highlightColor
    )

    override fun values(parameters: BrickTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.aspectRatio,
        parameters.mortarWidth,
        parameters.irregularity,
        parameters.roughness,
        parameters.bevel
    )
}
