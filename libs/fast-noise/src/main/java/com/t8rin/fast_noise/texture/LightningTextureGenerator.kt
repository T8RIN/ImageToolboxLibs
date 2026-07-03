package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LightningTextureParameters(override val seed: Int = 1337, val scale: Float = 0.006f, val branches: Float = 7f, val turbulence: Float = 42f, val width: Float = 0.055f, val glow: Float = 0.82f, val intensity: Float = 0.9f, val backgroundColor: Int = 0xFF030612.toInt(), val haloColor: Int = 0xFF2546A8.toInt(), val boltColor: Int = 0xFF68C9FF.toInt(), val coreColor: Int = 0xFFF2FCFF.toInt()) : ProceduralTextureParameters
class LightningTextureGenerator : NativeProceduralTextureGenerator<LightningTextureParameters>(TextureType.LIGHTNING) {
    override fun colors(parameters: LightningTextureParameters) = intArrayOf(parameters.backgroundColor, parameters.haloColor, parameters.boltColor, parameters.coreColor)
    override fun values(parameters: LightningTextureParameters) = floatArrayOf(parameters.scale, parameters.branches, parameters.turbulence, parameters.width, parameters.glow, parameters.intensity)
}
