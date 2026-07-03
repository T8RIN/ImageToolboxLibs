package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class DamascusTextureParameters(override val seed: Int = 1337, val scale: Float = 0.006f, val layers: Float = 22f, val folding: Float = 0.72f, val distortion: Float = 34f, val polish: Float = 0.58f, val contrast: Float = 1.4f, val darkSteelColor: Int = 0xFF171C20.toInt(), val steelColor: Int = 0xFF66727A.toInt(), val lightSteelColor: Int = 0xFFC8D0D2.toInt(), val oxideColor: Int = 0xFF304C57.toInt()) : ProceduralTextureParameters
class DamascusTextureGenerator : NativeProceduralTextureGenerator<DamascusTextureParameters>(TextureType.DAMASCUS) {
    override fun colors(parameters: DamascusTextureParameters) = intArrayOf(parameters.darkSteelColor, parameters.steelColor, parameters.lightSteelColor, parameters.oxideColor)
    override fun values(parameters: DamascusTextureParameters) = floatArrayOf(parameters.scale, parameters.layers, parameters.folding, parameters.distortion, parameters.polish, parameters.contrast)
}
