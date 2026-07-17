package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class EmberFieldTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val emberDensity: Float = 0.72f,
    val updraft: Float = 0.62f,
    val emberSharpness: Float = 0.76f,
    val glow: Float = 0.88f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF100504.toInt(),
    val primaryColor: Int = 0xFF76200B.toInt(),
    val secondaryColor: Int = 0xFFF05A14.toInt(),
    val highlightColor: Int = 0xFFFFE06B.toInt()
) : ProceduralTextureParameters

class EmberFieldTextureGenerator :
    NativeProceduralTextureGenerator<EmberFieldTextureParameters>(TextureType.EMBER_FIELD) {
    override fun colors(parameters: EmberFieldTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: EmberFieldTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.emberDensity,
        parameters.updraft,
        parameters.emberSharpness,
        parameters.glow,
        parameters.contrast
    )
}
