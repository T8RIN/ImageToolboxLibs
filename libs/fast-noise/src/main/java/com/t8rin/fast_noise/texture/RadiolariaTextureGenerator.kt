package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RadiolariaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val poreDensity: Float = 0.55f,
    val spines: Float = 0.48f,
    val shellWidth: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF092634.toInt(),
    val primaryColor: Int = 0xFF28778B.toInt(),
    val secondaryColor: Int = 0xFF8FCAC3.toInt(),
    val highlightColor: Int = 0xFFE7F1C9.toInt()
) : ProceduralTextureParameters

class RadiolariaTextureGenerator :
    NativeProceduralTextureGenerator<RadiolariaTextureParameters>(TextureType.RADIOLARIA) {
    override fun colors(parameters: RadiolariaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RadiolariaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.poreDensity,
        parameters.spines,
        parameters.shellWidth,
        parameters.glow,
        parameters.contrast
    )
}
