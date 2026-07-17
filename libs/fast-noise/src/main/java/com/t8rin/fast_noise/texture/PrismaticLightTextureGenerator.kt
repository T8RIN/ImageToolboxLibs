package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PrismaticLightTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val facets: Float = 0.66f,
    val dispersion: Float = 0.72f,
    val beamSharpness: Float = 0.76f,
    val bloom: Float = 0.78f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF07091A.toInt(),
    val primaryColor: Int = 0xFF246DD8.toInt(),
    val secondaryColor: Int = 0xFFE94DA9.toInt(),
    val highlightColor: Int = 0xFFFFE47A.toInt()
) : ProceduralTextureParameters

class PrismaticLightTextureGenerator :
    NativeProceduralTextureGenerator<PrismaticLightTextureParameters>(TextureType.PRISMATIC_LIGHT) {
    override fun colors(parameters: PrismaticLightTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PrismaticLightTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.facets,
        parameters.dispersion,
        parameters.beamSharpness,
        parameters.bloom,
        parameters.contrast
    )
}
