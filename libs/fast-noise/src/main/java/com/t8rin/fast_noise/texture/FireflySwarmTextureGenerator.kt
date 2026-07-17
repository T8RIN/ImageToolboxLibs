package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FireflySwarmTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val density: Float = 0.66f,
    val drift: Float = 0.72f,
    val trailSharpness: Float = 0.82f,
    val glow: Float = 0.90f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF020B12.toInt(),
    val primaryColor: Int = 0xFF0B4D49.toInt(),
    val secondaryColor: Int = 0xFF80D85A.toInt(),
    val highlightColor: Int = 0xFFFFFFB0.toInt()
) : ProceduralTextureParameters

class FireflySwarmTextureGenerator :
    NativeProceduralTextureGenerator<FireflySwarmTextureParameters>(TextureType.FIREFLY_SWARM) {
    override fun colors(parameters: FireflySwarmTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FireflySwarmTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.density,
        parameters.drift,
        parameters.trailSharpness,
        parameters.glow,
        parameters.contrast
    )
}
