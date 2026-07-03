package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TerrainTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.0045f,
    val waterLevel: Float = 0.34f,
    val mountainLevel: Float = 0.7f,
    val erosion: Float = 0.46f,
    val detail: Float = 0.58f,
    val snowLevel: Float = 0.86f,
    val waterColor: Int = 0xFF194D73.toInt(),
    val lowlandColor: Int = 0xFF5F873F.toInt(),
    val rockColor: Int = 0xFF746C5D.toInt(),
    val snowColor: Int = 0xFFE9EEE8.toInt()
) : ProceduralTextureParameters

class TerrainTextureGenerator :
    NativeProceduralTextureGenerator<TerrainTextureParameters>(TextureType.TERRAIN) {
    override fun colors(parameters: TerrainTextureParameters) = intArrayOf(
        parameters.waterColor,
        parameters.lowlandColor,
        parameters.rockColor,
        parameters.snowColor
    )

    override fun values(parameters: TerrainTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.waterLevel,
        parameters.mountainLevel,
        parameters.erosion,
        parameters.detail,
        parameters.snowLevel
    )
}
