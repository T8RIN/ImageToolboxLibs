package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BubbleWrapTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.02f,
    val bubbleRadius: Float = 0.7f,
    val relief: Float = 0.7f,
    val shine: Float = 0.75f,
    val rimWidth: Float = 0.45f,
    val highlights: Float = 0.8f,
    val backgroundColor: Int = 0xFF15364F.toInt(),
    val primaryColor: Int = 0xFF398AA6.toInt(),
    val secondaryColor: Int = 0xFFA3DBE3.toInt(),
    val highlightColor: Int = 0xFFF0FFFF.toInt()
) : ProceduralTextureParameters

class BubbleWrapTextureGenerator :
    NativeProceduralTextureGenerator<BubbleWrapTextureParameters>(TextureType.BUBBLE_WRAP) {
    override fun colors(parameters: BubbleWrapTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BubbleWrapTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bubbleRadius,
        parameters.relief,
        parameters.shine,
        parameters.rimWidth,
        parameters.highlights
    )
}
