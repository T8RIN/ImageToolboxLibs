package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MonsteraCanopyTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val leafDensity: Float = 0.55f,
    val lobes: Float = 0.48f,
    val fenestration: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0A241F.toInt(),
    val primaryColor: Int = 0xFF1C5741.toInt(),
    val secondaryColor: Int = 0xFF6C9B61.toInt(),
    val highlightColor: Int = 0xFFB8CE8F.toInt()
) : ProceduralTextureParameters

class MonsteraCanopyTextureGenerator :
    NativeProceduralTextureGenerator<MonsteraCanopyTextureParameters>(TextureType.MONSTERA_CANOPY) {
    override fun colors(parameters: MonsteraCanopyTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MonsteraCanopyTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.leafDensity,
        parameters.lobes,
        parameters.fenestration,
        parameters.light,
        parameters.contrast
    )
}
