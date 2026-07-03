package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GrassTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val bladeDensity: Float = 1f,
    val bladeLength: Float = 50f,
    val wind: Float = 1f,
    val patchiness: Float = 0.35f,
    val highlights: Float = 0.45f,
    val dirtColor: Int = 0xFF3A2B18.toInt(),
    val darkGrassColor: Int = 0xFF1D491F.toInt(),
    val grassColor: Int = 0xFF4D8A32.toInt(),
    val tipColor: Int = 0xFF9BC45A.toInt()
) : ProceduralTextureParameters

class GrassTextureGenerator :
    NativeProceduralTextureGenerator<GrassTextureParameters>(TextureType.GRASS) {
    override fun colors(parameters: GrassTextureParameters) = intArrayOf(
        parameters.dirtColor,
        parameters.darkGrassColor,
        parameters.grassColor,
        parameters.tipColor
    )

    override fun values(parameters: GrassTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bladeDensity,
        parameters.bladeLength,
        parameters.wind,
        parameters.patchiness,
        parameters.highlights
    )
}
