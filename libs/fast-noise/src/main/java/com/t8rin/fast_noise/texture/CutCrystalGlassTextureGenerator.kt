package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CutCrystalGlassTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val cutDensity: Float = 0.55f,
    val facets: Float = 0.48f,
    val relief: Float = 0.55f,
    val sparkle: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0A1D2D.toInt(),
    val primaryColor: Int = 0xFF3D7792.toInt(),
    val secondaryColor: Int = 0xFFA6CED6.toInt(),
    val highlightColor: Int = 0xFFF7F5DA.toInt()
) : ProceduralTextureParameters

class CutCrystalGlassTextureGenerator :
    NativeProceduralTextureGenerator<CutCrystalGlassTextureParameters>(TextureType.CUT_CRYSTAL_GLASS) {
    override fun colors(parameters: CutCrystalGlassTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CutCrystalGlassTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.cutDensity,
        parameters.facets,
        parameters.relief,
        parameters.sparkle,
        parameters.contrast
    )
}
