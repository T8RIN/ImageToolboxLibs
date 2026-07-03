package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FireTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val flameFrequency: Float = 8f,
    val turbulence: Float = 36f,
    val intensity: Float = 0.78f,
    val smoke: Float = 0.18f,
    val detail: Float = 0.62f,
    val backgroundColor: Int = 0xFF100807.toInt(),
    val redColor: Int = 0xFFB51B08.toInt(),
    val orangeColor: Int = 0xFFFF7A0A.toInt(),
    val coreColor: Int = 0xFFFFE98A.toInt()
) : ProceduralTextureParameters

class FireTextureGenerator :
    NativeProceduralTextureGenerator<FireTextureParameters>(TextureType.FIRE) {
    override fun colors(parameters: FireTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.redColor,
        parameters.orangeColor,
        parameters.coreColor
    )

    override fun values(parameters: FireTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.flameFrequency,
        parameters.turbulence,
        parameters.intensity,
        parameters.smoke,
        parameters.detail
    )
}
