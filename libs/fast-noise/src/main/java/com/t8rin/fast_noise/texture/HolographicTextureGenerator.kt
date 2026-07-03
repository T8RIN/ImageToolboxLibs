package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HolographicTextureParameters(override val seed: Int = 1337, val scale: Float = 0.008f, val spectrum: Float = 12f, val crinkles: Float = 0.7f, val diffraction: Float = 0.82f, val angle: Float = 0.3f, val shine: Float = 0.72f, val silverColor: Int = 0xFFCDD4DE.toInt(), val cyanColor: Int = 0xFF4DE5E2.toInt(), val magentaColor: Int = 0xFFE95AC8.toInt(), val yellowColor: Int = 0xFFF7EA72.toInt()) : ProceduralTextureParameters
class HolographicTextureGenerator : NativeProceduralTextureGenerator<HolographicTextureParameters>(TextureType.HOLOGRAPHIC) {
    override fun colors(parameters: HolographicTextureParameters) = intArrayOf(parameters.silverColor, parameters.cyanColor, parameters.magentaColor, parameters.yellowColor)
    override fun values(parameters: HolographicTextureParameters) = floatArrayOf(parameters.scale, parameters.spectrum, parameters.crinkles, parameters.diffraction, parameters.angle, parameters.shine)
}
