package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class NebulaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.004f,
    val turbulence: Float = 44f,
    val cloudDensity: Float = 0.64f,
    val stars: Float = 0.38f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.45f,
    val spaceColor: Int = 0xFF050713.toInt(),
    val violetColor: Int = 0xFF542577.toInt(),
    val blueColor: Int = 0xFF2367A2.toInt(),
    val glowColor: Int = 0xFFF2A4DF.toInt()
) : ProceduralTextureParameters

class NebulaTextureGenerator :
    NativeProceduralTextureGenerator<NebulaTextureParameters>(TextureType.NEBULA) {
    override fun colors(parameters: NebulaTextureParameters) = intArrayOf(
        parameters.spaceColor,
        parameters.violetColor,
        parameters.blueColor,
        parameters.glowColor
    )

    override fun values(parameters: NebulaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.turbulence,
        parameters.cloudDensity,
        parameters.stars,
        parameters.glow,
        parameters.contrast
    )
}
