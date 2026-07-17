package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SeismicInterferenceTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val sourceCount: Float = 0.64f,
    val phase: Float = 0.52f,
    val ridgeSharpness: Float = 0.78f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF070A12.toInt(),
    val primaryColor: Int = 0xFF315B8D.toInt(),
    val secondaryColor: Int = 0xFFE36A42.toInt(),
    val highlightColor: Int = 0xFFFFE39A.toInt()
) : ProceduralTextureParameters

class SeismicInterferenceTextureGenerator :
    NativeProceduralTextureGenerator<SeismicInterferenceTextureParameters>(TextureType.SEISMIC_INTERFERENCE) {
    override fun colors(parameters: SeismicInterferenceTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SeismicInterferenceTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.sourceCount,
        parameters.phase,
        parameters.ridgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
