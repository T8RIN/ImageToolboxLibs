package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MotherOfPearlTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val plateDensity: Float = 0.55f,
    val distortion: Float = 0.48f,
    val growthLines: Float = 0.55f,
    val iridescence: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF172F3B.toInt(),
    val primaryColor: Int = 0xFF95CBCB.toInt(),
    val secondaryColor: Int = 0xFFD7AECC.toInt(),
    val highlightColor: Int = 0xFFFFF4D8.toInt()
) : ProceduralTextureParameters

class MotherOfPearlTextureGenerator :
    NativeProceduralTextureGenerator<MotherOfPearlTextureParameters>(TextureType.MOTHER_OF_PEARL) {
    override fun colors(parameters: MotherOfPearlTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MotherOfPearlTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.plateDensity,
        parameters.distortion,
        parameters.growthLines,
        parameters.iridescence,
        parameters.contrast
    )
}
