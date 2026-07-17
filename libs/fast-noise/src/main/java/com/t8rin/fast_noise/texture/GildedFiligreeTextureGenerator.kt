package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GildedFiligreeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val curlDensity: Float = 0.68f,
    val ornament: Float = 0.46f,
    val lineSharpness: Float = 0.82f,
    val metallic: Float = 0.74f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF0A1019.toInt(),
    val primaryColor: Int = 0xFF163B58.toInt(),
    val secondaryColor: Int = 0xFFC38A2A.toInt(),
    val highlightColor: Int = 0xFFFFE49A.toInt()
) : ProceduralTextureParameters

class GildedFiligreeTextureGenerator :
    NativeProceduralTextureGenerator<GildedFiligreeTextureParameters>(TextureType.GILDED_FILIGREE) {
    override fun colors(parameters: GildedFiligreeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GildedFiligreeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.curlDensity,
        parameters.ornament,
        parameters.lineSharpness,
        parameters.metallic,
        parameters.contrast
    )
}
