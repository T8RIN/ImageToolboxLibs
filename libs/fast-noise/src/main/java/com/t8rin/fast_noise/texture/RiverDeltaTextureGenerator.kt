package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RiverDeltaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val channelDensity: Float = 0.68f,
    val erosion: Float = 0.78f,
    val channelSharpness: Float = 0.76f,
    val waterGlow: Float = 0.62f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF25190E.toInt(),
    val primaryColor: Int = 0xFF8B693B.toInt(),
    val secondaryColor: Int = 0xFF1688A5.toInt(),
    val highlightColor: Int = 0xFF8EDDE0.toInt()
) : ProceduralTextureParameters

class RiverDeltaTextureGenerator :
    NativeProceduralTextureGenerator<RiverDeltaTextureParameters>(TextureType.RIVER_DELTA) {
    override fun colors(parameters: RiverDeltaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RiverDeltaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.channelDensity,
        parameters.erosion,
        parameters.channelSharpness,
        parameters.waterGlow,
        parameters.contrast
    )
}
