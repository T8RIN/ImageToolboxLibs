package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CelticKnotworkTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val rotation: Float = 0.5f,
    val ribbonWidth: Float = 0.5f,
    val inlayWidth: Float = 0.4f,
    val weaveDepth: Float = 0.55f,
    val highlightWidth: Float = 0.4f,
    val backgroundColor: Int = 0xFF0B2526.toInt(),
    val primaryColor: Int = 0xFF287666.toInt(),
    val secondaryColor: Int = 0xFFC39140.toInt(),
    val highlightColor: Int = 0xFFFFE4A1.toInt()
) : ProceduralTextureParameters

class CelticKnotworkTextureGenerator :
    NativeProceduralTextureGenerator<CelticKnotworkTextureParameters>(TextureType.CELTIC_KNOTWORK) {
    override fun colors(parameters: CelticKnotworkTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CelticKnotworkTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.ribbonWidth,
        parameters.inlayWidth,
        parameters.weaveDepth,
        parameters.highlightWidth
    )
}
