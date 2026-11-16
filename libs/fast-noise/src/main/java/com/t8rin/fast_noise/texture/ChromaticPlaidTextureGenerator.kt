package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChromaticPlaidTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val stripeWidth: Float = 0.45f,
    val threadDensity: Float = 0.5f,
    val weaveDepth: Float = 0.6f,
    val rotation: Float = 0.5f,
    val contrast: Float = 1.0f,
    val backgroundColor: Int = 0xFFFFFF00.toInt(),
    val primaryColor: Int = 0xFFFF0000.toInt(),
    val secondaryColor: Int = 0xFFFF00FF.toInt(),
    val highlightColor: Int = 0xFF00FFFF.toInt()
) : ProceduralTextureParameters

class ChromaticPlaidTextureGenerator :
    NativeProceduralTextureGenerator<ChromaticPlaidTextureParameters>(TextureType.CHROMATIC_PLAID) {
    override fun colors(parameters: ChromaticPlaidTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ChromaticPlaidTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.stripeWidth,
        parameters.threadDensity,
        parameters.weaveDepth,
        parameters.rotation,
        parameters.contrast
    )
}
