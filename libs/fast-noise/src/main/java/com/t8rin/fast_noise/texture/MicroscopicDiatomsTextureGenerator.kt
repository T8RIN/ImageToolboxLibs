package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MicroscopicDiatomsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val colonyDensity: Float = 0.62f,
    val shellVariation: Float = 0.30f,
    val poreSharpness: Float = 0.74f,
    val luminescence: Float = 0.58f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF082431.toInt(),
    val primaryColor: Int = 0xFF1E7181.toInt(),
    val secondaryColor: Int = 0xFF74D8C9.toInt(),
    val highlightColor: Int = 0xFFFFE9A8.toInt()
) : ProceduralTextureParameters

class MicroscopicDiatomsTextureGenerator :
    NativeProceduralTextureGenerator<MicroscopicDiatomsTextureParameters>(TextureType.MICROSCOPIC_DIATOMS) {
    override fun colors(parameters: MicroscopicDiatomsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MicroscopicDiatomsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.colonyDensity,
        parameters.shellVariation,
        parameters.poreSharpness,
        parameters.luminescence,
        parameters.contrast
    )
}
