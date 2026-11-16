package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GyroidTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val periods: Float = 0.55f,
    val rotation: Float = 0.48f,
    val thickness: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF102336.toInt(),
    val primaryColor: Int = 0xFF498C9F.toInt(),
    val secondaryColor: Int = 0xFFB7839D.toInt(),
    val highlightColor: Int = 0xFFEDDCB0.toInt()
) : ProceduralTextureParameters

class GyroidTextureGenerator :
    NativeProceduralTextureGenerator<GyroidTextureParameters>(TextureType.GYROID) {
    override fun colors(parameters: GyroidTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GyroidTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.periods,
        parameters.rotation,
        parameters.thickness,
        parameters.polish,
        parameters.contrast
    )
}
