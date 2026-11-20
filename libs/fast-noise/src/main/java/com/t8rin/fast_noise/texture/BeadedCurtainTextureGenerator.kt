package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BeadedCurtainTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val dropDensity: Float = 0.68f,
    val streaking: Float = 0.58f,
    val dropSharpness: Float = 0.74f,
    val refraction: Float = 0.78f,
    val contrast: Float = 1.4f,
    val backgroundColor: Int = 0xFF071728.toInt(),
    val primaryColor: Int = 0xFF194C70.toInt(),
    val secondaryColor: Int = 0xFF69BDD2.toInt(),
    val highlightColor: Int = 0xFFDFFBFF.toInt()
) : ProceduralTextureParameters

class BeadedCurtainTextureGenerator :
    NativeProceduralTextureGenerator<BeadedCurtainTextureParameters>(TextureType.BEADED_CURTAIN) {
    override fun colors(parameters: BeadedCurtainTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BeadedCurtainTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.dropDensity,
        parameters.streaking,
        parameters.dropSharpness,
        parameters.refraction,
        parameters.contrast
    )
}
