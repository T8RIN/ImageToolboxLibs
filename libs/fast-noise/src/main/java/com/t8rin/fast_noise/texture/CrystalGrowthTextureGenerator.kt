package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CrystalGrowthTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val crystalDensity: Float = 0.64f,
    val anisotropy: Float = 0.48f,
    val facetSharpness: Float = 0.80f,
    val glow: Float = 0.78f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF101525.toInt(),
    val primaryColor: Int = 0xFF3F62A1.toInt(),
    val secondaryColor: Int = 0xFFA9D8EE.toInt(),
    val highlightColor: Int = 0xFFFFF0C2.toInt()
) : ProceduralTextureParameters

class CrystalGrowthTextureGenerator :
    NativeProceduralTextureGenerator<CrystalGrowthTextureParameters>(TextureType.CRYSTAL_GROWTH) {
    override fun colors(parameters: CrystalGrowthTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CrystalGrowthTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.crystalDensity,
        parameters.anisotropy,
        parameters.facetSharpness,
        parameters.glow,
        parameters.contrast
    )
}
