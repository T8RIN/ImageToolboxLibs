package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class VelvetTextureParameters(override val seed: Int = 1337, val scale: Float = 0.014f, val fibers: Float = 0.78f, val direction: Float = 0.18f, val softness: Float = 0.7f, val sheen: Float = 0.62f, val folds: Float = 0.34f, val shadowColor: Int = 0xFF17051E.toInt(), val velvetColor: Int = 0xFF5E176A.toInt(), val sheenColor: Int = 0xFFC148B8.toInt(), val highlightColor: Int = 0xFFF0A6D8.toInt()) : ProceduralTextureParameters
class VelvetTextureGenerator : NativeProceduralTextureGenerator<VelvetTextureParameters>(TextureType.VELVET) {
    override fun colors(parameters: VelvetTextureParameters) = intArrayOf(parameters.shadowColor, parameters.velvetColor, parameters.sheenColor, parameters.highlightColor)
    override fun values(parameters: VelvetTextureParameters) = floatArrayOf(parameters.scale, parameters.fibers, parameters.direction, parameters.softness, parameters.sheen, parameters.folds)
}
