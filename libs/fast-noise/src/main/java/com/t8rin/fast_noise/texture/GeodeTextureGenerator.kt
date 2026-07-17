package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GeodeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bands: Float = 0.68f,
    val distortion: Float = 0.55f,
    val crystalSharpness: Float = 0.72f,
    val sparkle: Float = 0.70f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF120E20.toInt(),
    val primaryColor: Int = 0xFF7046A8.toInt(),
    val secondaryColor: Int = 0xFF3ED7D1.toInt(),
    val highlightColor: Int = 0xFFF4DDA6.toInt()
) : ProceduralTextureParameters

class GeodeTextureGenerator :
    NativeProceduralTextureGenerator<GeodeTextureParameters>(TextureType.GEODE) {
    override fun colors(parameters: GeodeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GeodeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bands,
        parameters.distortion,
        parameters.crystalSharpness,
        parameters.sparkle,
        parameters.contrast
    )
}
