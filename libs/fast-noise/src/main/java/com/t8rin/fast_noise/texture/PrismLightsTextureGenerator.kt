package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PrismLightsTextureParameters(
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

class PrismLightsTextureGenerator :
    NativeProceduralTextureGenerator<PrismLightsTextureParameters>(TextureType.PRISM_LIGHTS) {
    override fun colors(parameters: PrismLightsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.coldColor,
        parameters.warmColor,
        parameters.coreColor
    )

    override fun values(parameters: PrismLightsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lobes,
        parameters.orbitDensity,
        parameters.curvature,
        parameters.thickness,
        parameters.glow
    )
}
