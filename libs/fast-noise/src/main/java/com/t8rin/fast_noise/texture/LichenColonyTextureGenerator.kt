package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LichenColonyTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val colonyDensity: Float = 0.64f,
    val spread: Float = 0.58f,
    val edgeSharpness: Float = 0.70f,
    val moisture: Float = 0.48f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF29291A.toInt(),
    val primaryColor: Int = 0xFF66743A.toInt(),
    val secondaryColor: Int = 0xFFB1B85B.toInt(),
    val highlightColor: Int = 0xFFE0D59A.toInt()
) : ProceduralTextureParameters

class LichenColonyTextureGenerator :
    NativeProceduralTextureGenerator<LichenColonyTextureParameters>(TextureType.LICHEN_COLONY) {
    override fun colors(parameters: LichenColonyTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LichenColonyTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.colonyDensity,
        parameters.spread,
        parameters.edgeSharpness,
        parameters.moisture,
        parameters.contrast
    )
}
