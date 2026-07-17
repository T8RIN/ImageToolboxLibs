package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MoireGuillocheTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val frequency: Float = 0.55f,
    val offset: Float = 0.30f,
    val lineSharpness: Float = 0.72f,
    val glow: Float = 0.48f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF070914.toInt(),
    val primaryColor: Int = 0xFF2747A5.toInt(),
    val secondaryColor: Int = 0xFFBD4BC8.toInt(),
    val highlightColor: Int = 0xFFF2EAFB.toInt()
) : ProceduralTextureParameters

class MoireGuillocheTextureGenerator :
    NativeProceduralTextureGenerator<MoireGuillocheTextureParameters>(TextureType.MOIRE_GUILLOCHE) {
    override fun colors(parameters: MoireGuillocheTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MoireGuillocheTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.frequency,
        parameters.offset,
        parameters.lineSharpness,
        parameters.glow,
        parameters.contrast
    )
}
