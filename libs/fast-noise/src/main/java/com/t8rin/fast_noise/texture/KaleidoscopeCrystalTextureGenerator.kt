package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class KaleidoscopeCrystalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val segments: Float = 0.8f,
    val foldWarp: Float = 0.58f,
    val facetSharpness: Float = 0.84f,
    val glow: Float = 0.56f,
    val contrast: Float = 1.6f,
    val backgroundColor: Int = 0xFF090B1C.toInt(),
    val primaryColor: Int = 0xFF2367B7.toInt(),
    val secondaryColor: Int = 0xFFE04DA4.toInt(),
    val highlightColor: Int = 0xFF62F5CA.toInt()
) : ProceduralTextureParameters

class KaleidoscopeCrystalTextureGenerator :
    NativeProceduralTextureGenerator<KaleidoscopeCrystalTextureParameters>(TextureType.KALEIDOSCOPE_CRYSTAL) {
    override fun colors(parameters: KaleidoscopeCrystalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: KaleidoscopeCrystalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.segments,
        parameters.foldWarp,
        parameters.facetSharpness,
        parameters.glow,
        parameters.contrast
    )
}
