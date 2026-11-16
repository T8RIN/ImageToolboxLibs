package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SashikoTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val stitchDensity: Float = 0.55f,
    val stitchLength: Float = 0.48f,
    val threadWidth: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0B1C37.toInt(),
    val primaryColor: Int = 0xFF244768.toInt(),
    val secondaryColor: Int = 0xFFC6D5D7.toInt(),
    val highlightColor: Int = 0xFFF2DBB0.toInt()
) : ProceduralTextureParameters

class SashikoTextureGenerator :
    NativeProceduralTextureGenerator<SashikoTextureParameters>(TextureType.SASHIKO) {
    override fun colors(parameters: SashikoTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SashikoTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.stitchDensity,
        parameters.stitchLength,
        parameters.threadWidth,
        parameters.relief,
        parameters.contrast
    )
}
