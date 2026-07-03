package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HoneycombTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val borderWidth: Float = 0.095f,
    val bevel: Float = 0.5f,
    val irregularity: Float = 0.12f,
    val fill: Float = 0.72f,
    val glow: Float = 0.35f,
    val backgroundColor: Int = 0xFF241305.toInt(),
    val borderColor: Int = 0xFF6F3A08.toInt(),
    val honeyColor: Int = 0xFFE59B12.toInt(),
    val highlightColor: Int = 0xFFFFD45C.toInt()
) : ProceduralTextureParameters

class HoneycombTextureGenerator :
    NativeProceduralTextureGenerator<HoneycombTextureParameters>(TextureType.HONEYCOMB) {
    override fun colors(parameters: HoneycombTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.borderColor,
        parameters.honeyColor,
        parameters.highlightColor
    )

    override fun values(parameters: HoneycombTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.borderWidth,
        parameters.bevel,
        parameters.irregularity,
        parameters.fill,
        parameters.glow
    )
}
