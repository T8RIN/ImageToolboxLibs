package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HopfFibrationTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val fiberCount: Float = 0.55f,
    val latitude: Float = 0.48f,
    val thickness: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0A1930.toInt(),
    val primaryColor: Int = 0xFF338DA5.toInt(),
    val secondaryColor: Int = 0xFFD77B9D.toInt(),
    val highlightColor: Int = 0xFFEED293.toInt()
) : ProceduralTextureParameters

class HopfFibrationTextureGenerator :
    NativeProceduralTextureGenerator<HopfFibrationTextureParameters>(TextureType.HOPF_FIBRATION) {
    override fun colors(parameters: HopfFibrationTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HopfFibrationTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.fiberCount,
        parameters.latitude,
        parameters.thickness,
        parameters.glow,
        parameters.contrast
    )
}
