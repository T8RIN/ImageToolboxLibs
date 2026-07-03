package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TopographyTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.0055f,
    val lineCount: Float = 16f,
    val lineThickness: Float = 0.11f,
    val shading: Float = 0.42f,
    val distortion: Float = 12f,
    val contrast: Float = 1.1f,
    val lowColor: Int = 0xFF173C3B.toInt(),
    val highColor: Int = 0xFFB5C98A.toInt(),
    val lineColor: Int = 0xFFF2E6B6.toInt()
) : ProceduralTextureParameters

class TopographyTextureGenerator :
    NativeProceduralTextureGenerator<TopographyTextureParameters>(TextureType.TOPOGRAPHY) {
    override fun colors(parameters: TopographyTextureParameters) = intArrayOf(
        parameters.lowColor,
        parameters.highColor,
        parameters.lineColor
    )

    override fun values(parameters: TopographyTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lineCount,
        parameters.lineThickness,
        parameters.shading,
        parameters.distortion,
        parameters.contrast
    )
}
