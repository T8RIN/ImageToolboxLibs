package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class EmbossedPaisleyTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val motifDensity: Float = 0.55f,
    val curl: Float = 0.48f,
    val lineWidth: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF201A30.toInt(),
    val primaryColor: Int = 0xFF73506F.toInt(),
    val secondaryColor: Int = 0xFFC29863.toInt(),
    val highlightColor: Int = 0xFFF2D5A1.toInt()
) : ProceduralTextureParameters

class EmbossedPaisleyTextureGenerator :
    NativeProceduralTextureGenerator<EmbossedPaisleyTextureParameters>(TextureType.EMBOSSED_PAISLEY) {
    override fun colors(parameters: EmbossedPaisleyTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: EmbossedPaisleyTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.motifDensity,
        parameters.curl,
        parameters.lineWidth,
        parameters.relief,
        parameters.contrast
    )
}
