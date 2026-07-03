package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CosmicVortexTextureParameters(override val seed: Int = 1337, val scale: Float = 0.008f, val arms: Float = 5f, val twist: Float = 12f, val turbulence: Float = 0.52f, val stars: Float = 0.28f, val coreGlow: Float = 0.82f, val spaceColor: Int = 0xFF02030B.toInt(), val blueColor: Int = 0xFF274AA8.toInt(), val violetColor: Int = 0xFF8D3EC2.toInt(), val coreColor: Int = 0xFFFFDDA0.toInt()) : ProceduralTextureParameters
class CosmicVortexTextureGenerator : NativeProceduralTextureGenerator<CosmicVortexTextureParameters>(TextureType.COSMIC_VORTEX) {
    override fun colors(parameters: CosmicVortexTextureParameters) = intArrayOf(parameters.spaceColor, parameters.blueColor, parameters.violetColor, parameters.coreColor)
    override fun values(parameters: CosmicVortexTextureParameters) = floatArrayOf(parameters.scale, parameters.arms, parameters.twist, parameters.turbulence, parameters.stars, parameters.coreGlow)
}
