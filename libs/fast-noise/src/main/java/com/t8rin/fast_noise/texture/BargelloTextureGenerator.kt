package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BargelloTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val flameCount: Float = 0.55f,
    val flameHeight: Float = 0.48f,
    val stitchWidth: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF25213A.toInt(),
    val primaryColor: Int = 0xFFCE6473.toInt(),
    val secondaryColor: Int = 0xFF3A8C9A.toInt(),
    val highlightColor: Int = 0xFFF2D998.toInt()
) : ProceduralTextureParameters

class BargelloTextureGenerator :
    NativeProceduralTextureGenerator<BargelloTextureParameters>(TextureType.BARGELLO) {
    override fun colors(parameters: BargelloTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BargelloTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.flameCount,
        parameters.flameHeight,
        parameters.stitchWidth,
        parameters.relief,
        parameters.contrast
    )
}
