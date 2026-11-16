package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SpirographTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val lobes: Float = 0.55f,
    val ratio: Float = 0.48f,
    val lineWidth: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF111A30.toInt(),
    val primaryColor: Int = 0xFF339DAD.toInt(),
    val secondaryColor: Int = 0xFFDA7FA9.toInt(),
    val highlightColor: Int = 0xFFF4D48D.toInt()
) : ProceduralTextureParameters

class SpirographTextureGenerator :
    NativeProceduralTextureGenerator<SpirographTextureParameters>(TextureType.SPIROGRAPH) {
    override fun colors(parameters: SpirographTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SpirographTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lobes,
        parameters.ratio,
        parameters.lineWidth,
        parameters.glow,
        parameters.contrast
    )
}
