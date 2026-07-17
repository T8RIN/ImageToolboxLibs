package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LiquidCrystalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val domains: Float = 0.66f,
    val dispersion: Float = 0.62f,
    val boundarySharpness: Float = 0.58f,
    val shine: Float = 0.72f,
    val contrast: Float = 1.30f,
    val backgroundColor: Int = 0xFF090A18.toInt(),
    val primaryColor: Int = 0xFF18BFD2.toInt(),
    val secondaryColor: Int = 0xFFE142A9.toInt(),
    val highlightColor: Int = 0xFFFFE96A.toInt()
) : ProceduralTextureParameters

class LiquidCrystalTextureGenerator :
    NativeProceduralTextureGenerator<LiquidCrystalTextureParameters>(TextureType.LIQUID_CRYSTAL) {
    override fun colors(parameters: LiquidCrystalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LiquidCrystalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.domains,
        parameters.dispersion,
        parameters.boundarySharpness,
        parameters.shine,
        parameters.contrast
    )
}
