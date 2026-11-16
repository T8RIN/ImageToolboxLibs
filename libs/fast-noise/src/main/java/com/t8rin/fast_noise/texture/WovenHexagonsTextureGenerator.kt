package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class WovenHexagonsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val weaveDensity: Float = 0.55f,
    val ribbonWidth: Float = 0.48f,
    val twist: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF132E35.toInt(),
    val primaryColor: Int = 0xFF2D7580.toInt(),
    val secondaryColor: Int = 0xFFD19B61.toInt(),
    val highlightColor: Int = 0xFFF3DBA7.toInt()
) : ProceduralTextureParameters

class WovenHexagonsTextureGenerator :
    NativeProceduralTextureGenerator<WovenHexagonsTextureParameters>(TextureType.WOVEN_HEXAGONS) {
    override fun colors(parameters: WovenHexagonsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: WovenHexagonsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.weaveDensity,
        parameters.ribbonWidth,
        parameters.twist,
        parameters.relief,
        parameters.contrast
    )
}
