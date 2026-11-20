package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FrostSpindleTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branches: Float = 0.78f,
    val branching: Float = 0.62f,
    val crystalSharpness: Float = 0.82f,
    val frostGlow: Float = 0.68f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF10273A.toInt(),
    val primaryColor: Int = 0xFF559AB4.toInt(),
    val secondaryColor: Int = 0xFFB8E8ED.toInt(),
    val highlightColor: Int = 0xFFE8FBFF.toInt()
) : ProceduralTextureParameters

class FrostSpindleTextureGenerator :
    NativeProceduralTextureGenerator<FrostSpindleTextureParameters>(TextureType.FROST_SPINDLE) {
    override fun colors(parameters: FrostSpindleTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FrostSpindleTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branches,
        parameters.branching,
        parameters.crystalSharpness,
        parameters.frostGlow,
        parameters.contrast
    )
}
