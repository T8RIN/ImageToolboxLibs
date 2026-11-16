package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class KilimTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val diamondDensity: Float = 0.55f,
    val stepping: Float = 0.48f,
    val borderWidth: Float = 0.55f,
    val weave: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF281D26.toInt(),
    val primaryColor: Int = 0xFFB94B39.toInt(),
    val secondaryColor: Int = 0xFF2C7477.toInt(),
    val highlightColor: Int = 0xFFEDD3A2.toInt()
) : ProceduralTextureParameters

class KilimTextureGenerator :
    NativeProceduralTextureGenerator<KilimTextureParameters>(TextureType.KILIM) {
    override fun colors(parameters: KilimTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: KilimTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.diamondDensity,
        parameters.stepping,
        parameters.borderWidth,
        parameters.weave,
        parameters.contrast
    )
}
