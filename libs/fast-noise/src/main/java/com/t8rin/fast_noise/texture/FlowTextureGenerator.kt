package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FlowTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val frequency: Float = 12f,
    val distortion: Float = 46f,
    val symmetry: Float = 0.3f,
    val sharpness: Float = 1.25f,
    val glow: Float = 0.62f,
    val backgroundColor: Int = 0xFF111128.toInt(),
    val firstColor: Int = 0xFF5155D9.toInt(),
    val secondColor: Int = 0xFFDE4BB3.toInt(),
    val glowColor: Int = 0xFF7EF2E7.toInt()
) : ProceduralTextureParameters

class FlowTextureGenerator :
    NativeProceduralTextureGenerator<FlowTextureParameters>(TextureType.FLOW) {
    override fun colors(parameters: FlowTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.firstColor,
        parameters.secondColor,
        parameters.glowColor
    )

    override fun values(parameters: FlowTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.frequency,
        parameters.distortion,
        parameters.symmetry,
        parameters.sharpness,
        parameters.glow
    )
}
