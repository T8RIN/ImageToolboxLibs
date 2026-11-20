package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SpiralCobwebTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branchCount: Float = 0.72f,
    val branching: Float = 0.66f,
    val arcSharpness: Float = 0.84f,
    val glow: Float = 0.9f,
    val contrast: Float = 1.6f,
    val backgroundColor: Int = 0xFF08030C.toInt(),
    val primaryColor: Int = 0xFF5E176E.toInt(),
    val secondaryColor: Int = 0xFFE544A7.toInt(),
    val highlightColor: Int = 0xFFFFE9FF.toInt()
) : ProceduralTextureParameters

class SpiralCobwebTextureGenerator :
    NativeProceduralTextureGenerator<SpiralCobwebTextureParameters>(TextureType.SPIRAL_COBWEB) {
    override fun colors(parameters: SpiralCobwebTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SpiralCobwebTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branchCount,
        parameters.branching,
        parameters.arcSharpness,
        parameters.glow,
        parameters.contrast
    )
}
