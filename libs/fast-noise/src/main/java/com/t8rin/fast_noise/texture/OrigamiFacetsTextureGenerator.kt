package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class OrigamiFacetsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val foldDensity: Float = 0.66f,
    val asymmetry: Float = 0.52f,
    val creaseSharpness: Float = 0.82f,
    val sheen: Float = 0.62f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF10121C.toInt(),
    val primaryColor: Int = 0xFF58678B.toInt(),
    val secondaryColor: Int = 0xFFB9C3D5.toInt(),
    val highlightColor: Int = 0xFFFFE4A8.toInt()
) : ProceduralTextureParameters

class OrigamiFacetsTextureGenerator :
    NativeProceduralTextureGenerator<OrigamiFacetsTextureParameters>(TextureType.ORIGAMI_FACETS) {
    override fun colors(parameters: OrigamiFacetsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: OrigamiFacetsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.foldDensity,
        parameters.asymmetry,
        parameters.creaseSharpness,
        parameters.sheen,
        parameters.contrast
    )
}
