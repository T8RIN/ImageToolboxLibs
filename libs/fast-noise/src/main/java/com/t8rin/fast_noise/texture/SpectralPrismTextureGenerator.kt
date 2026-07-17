package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SpectralPrismTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val facetCount: Float = 0.64f,
    val refraction: Float = 0.58f,
    val edgeSharpness: Float = 0.80f,
    val bloom: Float = 0.80f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF080A12.toInt(),
    val primaryColor: Int = 0xFF225CD1.toInt(),
    val secondaryColor: Int = 0xFFDD3DAD.toInt(),
    val highlightColor: Int = 0xFFFFE46A.toInt()
) : ProceduralTextureParameters

class SpectralPrismTextureGenerator :
    NativeProceduralTextureGenerator<SpectralPrismTextureParameters>(TextureType.SPECTRAL_PRISM) {
    override fun colors(parameters: SpectralPrismTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SpectralPrismTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.facetCount,
        parameters.refraction,
        parameters.edgeSharpness,
        parameters.bloom,
        parameters.contrast
    )
}
