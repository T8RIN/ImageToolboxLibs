package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GlacialCrevassesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val fractureDensity: Float = 0.55f,
    val warping: Float = 0.48f,
    val depth: Float = 0.55f,
    val frost: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0A2942.toInt(),
    val primaryColor: Int = 0xFF2A718F.toInt(),
    val secondaryColor: Int = 0xFF83BDD0.toInt(),
    val highlightColor: Int = 0xFFE4F0DF.toInt()
) : ProceduralTextureParameters

class GlacialCrevassesTextureGenerator :
    NativeProceduralTextureGenerator<GlacialCrevassesTextureParameters>(TextureType.GLACIAL_CREVASSES) {
    override fun colors(parameters: GlacialCrevassesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GlacialCrevassesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.fractureDensity,
        parameters.warping,
        parameters.depth,
        parameters.frost,
        parameters.contrast
    )
}
