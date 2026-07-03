package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class WaterRippleTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.0075f,
    val frequency: Float = 22f,
    val distortion: Float = 22f,
    val caustics: Float = 0.58f,
    val depth: Float = 0.55f,
    val highlights: Float = 0.62f,
    val deepColor: Int = 0xFF073456.toInt(),
    val shallowColor: Int = 0xFF168EAE.toInt(),
    val highlightColor: Int = 0xFFB9F7F2.toInt()
) : ProceduralTextureParameters

class WaterRippleTextureGenerator :
    NativeProceduralTextureGenerator<WaterRippleTextureParameters>(TextureType.WATER_RIPPLES) {
    override fun colors(parameters: WaterRippleTextureParameters) = intArrayOf(
        parameters.deepColor,
        parameters.shallowColor,
        parameters.highlightColor
    )

    override fun values(parameters: WaterRippleTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.frequency,
        parameters.distortion,
        parameters.caustics,
        parameters.depth,
        parameters.highlights
    )
}
