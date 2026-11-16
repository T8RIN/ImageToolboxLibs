package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PorcelainCrackleTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val crackDensity: Float = 0.55f,
    val irregularity: Float = 0.48f,
    val crackWidth: Float = 0.55f,
    val glaze: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF1D4157.toInt(),
    val primaryColor: Int = 0xFFD8E4DE.toInt(),
    val secondaryColor: Int = 0xFF9FBFCA.toInt(),
    val highlightColor: Int = 0xFFFFF6DD.toInt()
) : ProceduralTextureParameters

class PorcelainCrackleTextureGenerator :
    NativeProceduralTextureGenerator<PorcelainCrackleTextureParameters>(TextureType.PORCELAIN_CRACKLE) {
    override fun colors(parameters: PorcelainCrackleTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PorcelainCrackleTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.crackDensity,
        parameters.irregularity,
        parameters.crackWidth,
        parameters.glaze,
        parameters.contrast
    )
}
