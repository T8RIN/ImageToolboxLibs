package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LenticularWavesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val lensDensity: Float = 0.55f,
    val phase: Float = 0.48f,
    val curvature: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF162F49.toInt(),
    val primaryColor: Int = 0xFF4A96A8.toInt(),
    val secondaryColor: Int = 0xFFD78E8D.toInt(),
    val highlightColor: Int = 0xFFF2DBAA.toInt()
) : ProceduralTextureParameters

class LenticularWavesTextureGenerator :
    NativeProceduralTextureGenerator<LenticularWavesTextureParameters>(TextureType.LENTICULAR_WAVES) {
    override fun colors(parameters: LenticularWavesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LenticularWavesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lensDensity,
        parameters.phase,
        parameters.curvature,
        parameters.relief,
        parameters.contrast
    )
}
