package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SeraphiniteTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val plumeDensity: Float = 0.55f,
    val curvature: Float = 0.48f,
    val fibers: Float = 0.55f,
    val sheen: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF071E1B.toInt(),
    val primaryColor: Int = 0xFF245541.toInt(),
    val secondaryColor: Int = 0xFF9EBEAB.toInt(),
    val highlightColor: Int = 0xFFE7F2D4.toInt()
) : ProceduralTextureParameters

class SeraphiniteTextureGenerator :
    NativeProceduralTextureGenerator<SeraphiniteTextureParameters>(TextureType.SERAPHINITE) {
    override fun colors(parameters: SeraphiniteTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SeraphiniteTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.plumeDensity,
        parameters.curvature,
        parameters.fibers,
        parameters.sheen,
        parameters.contrast
    )
}
