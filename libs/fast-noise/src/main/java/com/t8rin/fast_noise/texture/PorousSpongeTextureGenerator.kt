package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PorousSpongeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val poreDensity: Float = 0.64f,
    val irregularity: Float = 0.42f,
    val poreSharpness: Float = 0.72f,
    val moisture: Float = 0.42f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF5B3718.toInt(),
    val primaryColor: Int = 0xFFB16C27.toInt(),
    val secondaryColor: Int = 0xFFE5B24E.toInt(),
    val highlightColor: Int = 0xFFFFE2A0.toInt()
) : ProceduralTextureParameters

class PorousSpongeTextureGenerator :
    NativeProceduralTextureGenerator<PorousSpongeTextureParameters>(TextureType.POROUS_SPONGE) {
    override fun colors(parameters: PorousSpongeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PorousSpongeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.poreDensity,
        parameters.irregularity,
        parameters.poreSharpness,
        parameters.moisture,
        parameters.contrast
    )
}
