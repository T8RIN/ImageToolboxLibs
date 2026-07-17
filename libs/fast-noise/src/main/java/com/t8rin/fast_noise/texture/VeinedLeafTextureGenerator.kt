package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class VeinedLeafTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val veinDensity: Float = 0.68f,
    val curvature: Float = 0.52f,
    val veinSharpness: Float = 0.78f,
    val translucency: Float = 0.62f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFF07180C.toInt(),
    val primaryColor: Int = 0xFF1E6732.toInt(),
    val secondaryColor: Int = 0xFF75BC50.toInt(),
    val highlightColor: Int = 0xFFD6E782.toInt()
) : ProceduralTextureParameters

class VeinedLeafTextureGenerator :
    NativeProceduralTextureGenerator<VeinedLeafTextureParameters>(TextureType.VEINED_LEAF) {
    override fun colors(parameters: VeinedLeafTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: VeinedLeafTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.veinDensity,
        parameters.curvature,
        parameters.veinSharpness,
        parameters.translucency,
        parameters.contrast
    )
}
