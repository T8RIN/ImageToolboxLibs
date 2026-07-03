package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class StrangeAttractorTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val lobes: Float = 3f,
    val orbitDensity: Float = 18f,
    val curvature: Float = 6f,
    val thickness: Float = 0.035f,
    val glow: Float = 0.8f,
    val backgroundColor: Int = 0xFF030511.toInt(),
    val coldColor: Int = 0xFF14D9C5.toInt(),
    val warmColor: Int = 0xFFFF3A79.toInt(),
    val coreColor: Int = 0xFFFFF1B5.toInt()
) : ProceduralTextureParameters

class StrangeAttractorTextureGenerator :
    NativeProceduralTextureGenerator<StrangeAttractorTextureParameters>(TextureType.STRANGE_ATTRACTOR) {
    override fun colors(parameters: StrangeAttractorTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.coldColor,
        parameters.warmColor,
        parameters.coreColor
    )

    override fun values(parameters: StrangeAttractorTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lobes,
        parameters.orbitDensity,
        parameters.curvature,
        parameters.thickness,
        parameters.glow
    )
}
