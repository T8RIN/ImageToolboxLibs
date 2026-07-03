package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RingedPlanetTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val planetSize: Float = 0.24f,
    val ringTilt: Float = 0.72f,
    val ringWidth: Float = 0.16f,
    val atmosphere: Float = 0.62f,
    val stars: Float = 0.25f,
    val spaceColor: Int = 0xFF02040D.toInt(),
    val shadowColor: Int = 0xFF15234D.toInt(),
    val planetColor: Int = 0xFFE58C62.toInt(),
    val ringColor: Int = 0xFFFFE1A6.toInt()
) : ProceduralTextureParameters

class RingedPlanetTextureGenerator :
    NativeProceduralTextureGenerator<RingedPlanetTextureParameters>(TextureType.RINGED_PLANET) {
    override fun colors(parameters: RingedPlanetTextureParameters) = intArrayOf(
        parameters.spaceColor,
        parameters.shadowColor,
        parameters.planetColor,
        parameters.ringColor
    )

    override fun values(parameters: RingedPlanetTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.planetSize,
        parameters.ringTilt,
        parameters.ringWidth,
        parameters.atmosphere,
        parameters.stars
    )
}
