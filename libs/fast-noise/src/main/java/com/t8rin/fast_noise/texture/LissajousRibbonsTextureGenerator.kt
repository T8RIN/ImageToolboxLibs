package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LissajousRibbonsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val frequencyX: Float = 3f,
    val frequencyY: Float = 4f,
    val phase: Float = 0.12f,
    val ribbonWidth: Float = 0.45f,
    val glow: Float = 0.65f,
    val backgroundColor: Int = 0xFF080E21.toInt(),
    val primaryColor: Int = 0xFF1CC6D0.toInt(),
    val secondaryColor: Int = 0xFFEC589B.toInt(),
    val highlightColor: Int = 0xFFFFE594.toInt()
) : ProceduralTextureParameters

class LissajousRibbonsTextureGenerator :
    NativeProceduralTextureGenerator<LissajousRibbonsTextureParameters>(TextureType.LISSAJOUS_RIBBONS) {
    override fun colors(parameters: LissajousRibbonsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LissajousRibbonsTextureParameters) = floatArrayOf(
        parameters.scale,
        (parameters.frequencyX - 1f) / 8f,
        (parameters.frequencyY - 1f) / 8f,
        parameters.phase,
        parameters.ribbonWidth,
        parameters.glow
    )
}
