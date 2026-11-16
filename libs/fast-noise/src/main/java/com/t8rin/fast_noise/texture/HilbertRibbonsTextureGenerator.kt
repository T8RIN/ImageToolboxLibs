package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HilbertRibbonsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val order: Float = 4f,
    val ribbonWidth: Float = 0.65f,
    val glow: Float = 0.6f,
    val rotation: Float = 0f,
    val highlight: Float = 0.7f,
    val backgroundColor: Int = 0xFF0C1429.toInt(),
    val primaryColor: Int = 0xFF278BD2.toInt(),
    val secondaryColor: Int = 0xFFD747AB.toInt(),
    val highlightColor: Int = 0xFFFFD878.toInt()
) : ProceduralTextureParameters

class HilbertRibbonsTextureGenerator :
    NativeProceduralTextureGenerator<HilbertRibbonsTextureParameters>(TextureType.HILBERT_RIBBONS) {
    override fun colors(parameters: HilbertRibbonsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HilbertRibbonsTextureParameters) = floatArrayOf(
        parameters.scale,
        (parameters.order - 2f) / 5f,
        parameters.ribbonWidth,
        parameters.glow,
        parameters.rotation / 3f,
        parameters.highlight
    )
}
