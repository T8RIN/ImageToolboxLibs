package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RakuGlazeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val islandDensity: Float = 0.55f,
    val distortion: Float = 0.48f,
    val crackle: Float = 0.55f,
    val iridescence: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF171916.toInt(),
    val primaryColor: Int = 0xFF92553E.toInt(),
    val secondaryColor: Int = 0xFF508D8A.toInt(),
    val highlightColor: Int = 0xFFEAD19C.toInt()
) : ProceduralTextureParameters

class RakuGlazeTextureGenerator :
    NativeProceduralTextureGenerator<RakuGlazeTextureParameters>(TextureType.RAKU_GLAZE) {
    override fun colors(parameters: RakuGlazeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RakuGlazeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.islandDensity,
        parameters.distortion,
        parameters.crackle,
        parameters.iridescence,
        parameters.contrast
    )
}
