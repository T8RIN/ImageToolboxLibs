package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TreeBarkTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.016f,
    val distortion: Float = 0.7f,
    val furrowDepth: Float = 0.4f,
    val detail: Float = 0.65f,
    val cracks: Float = 0.5f,
    val lichen: Float = 0.35f,
    val backgroundColor: Int = 0xFF221B16.toInt(),
    val primaryColor: Int = 0xFF796043.toInt(),
    val secondaryColor: Int = 0xFF647647.toInt(),
    val highlightColor: Int = 0xFFC9B98B.toInt()
) : ProceduralTextureParameters

class TreeBarkTextureGenerator :
    NativeProceduralTextureGenerator<TreeBarkTextureParameters>(TextureType.TREE_BARK) {
    override fun colors(parameters: TreeBarkTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TreeBarkTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.distortion,
        parameters.furrowDepth,
        parameters.detail,
        parameters.cracks,
        parameters.lichen
    )
}
