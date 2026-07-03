package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class OpalTextureParameters(override val seed: Int = 1337, val scale: Float = 0.007f, val colorPlay: Float = 0.82f, val milkiness: Float = 0.48f, val bands: Float = 8f, val distortion: Float = 28f, val glow: Float = 0.62f, val baseColor: Int = 0xFFDDE8DF.toInt(), val cyanColor: Int = 0xFF4FD5D1.toInt(), val pinkColor: Int = 0xFFF072B6.toInt(), val goldColor: Int = 0xFFFFC857.toInt()) : ProceduralTextureParameters
class OpalTextureGenerator : NativeProceduralTextureGenerator<OpalTextureParameters>(TextureType.OPAL) {
    override fun colors(parameters: OpalTextureParameters) = intArrayOf(parameters.baseColor, parameters.cyanColor, parameters.pinkColor, parameters.goldColor)
    override fun values(parameters: OpalTextureParameters) = floatArrayOf(parameters.scale, parameters.colorPlay, parameters.milkiness, parameters.bands, parameters.distortion, parameters.glow)
}
