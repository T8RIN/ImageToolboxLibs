package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ArtDecoFansTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val fanDensity: Float = 0.55f,
    val ribs: Float = 0.48f,
    val lineWidth: Float = 0.55f,
    val metallic: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF151F2A.toInt(),
    val primaryColor: Int = 0xFF37515A.toInt(),
    val secondaryColor: Int = 0xFFAE8551.toInt(),
    val highlightColor: Int = 0xFFF1D7A1.toInt()
) : ProceduralTextureParameters

class ArtDecoFansTextureGenerator :
    NativeProceduralTextureGenerator<ArtDecoFansTextureParameters>(TextureType.ART_DECO_FANS) {
    override fun colors(parameters: ArtDecoFansTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ArtDecoFansTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.fanDensity,
        parameters.ribs,
        parameters.lineWidth,
        parameters.metallic,
        parameters.contrast
    )
}
