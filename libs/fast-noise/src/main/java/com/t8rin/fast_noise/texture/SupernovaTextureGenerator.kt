package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SupernovaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val shockRadius: Float = 0.27f,
    val shellWidth: Float = 0.075f,
    val ejecta: Float = 0.72f,
    val turbulence: Float = 0.68f,
    val stars: Float = 0.24f,
    val spaceColor: Int = 0xFF02030B.toInt(),
    val cloudColor: Int = 0xFF6923B7.toInt(),
    val flameColor: Int = 0xFFFF4D22.toInt(),
    val coreColor: Int = 0xFFFFF2C5.toInt()
) : ProceduralTextureParameters

class SupernovaTextureGenerator :
    NativeProceduralTextureGenerator<SupernovaTextureParameters>(TextureType.SUPERNOVA) {
    override fun colors(parameters: SupernovaTextureParameters) = intArrayOf(
        parameters.spaceColor,
        parameters.cloudColor,
        parameters.flameColor,
        parameters.coreColor
    )

    override fun values(parameters: SupernovaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.shockRadius,
        parameters.shellWidth,
        parameters.ejecta,
        parameters.turbulence,
        parameters.stars
    )
}
