package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MagneticFieldTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val lineDensity: Float = 0.68f,
    val poleDistortion: Float = 0.42f,
    val lineSharpness: Float = 0.74f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFF070A17.toInt(),
    val primaryColor: Int = 0xFF2452A0.toInt(),
    val secondaryColor: Int = 0xFFE23D70.toInt(),
    val highlightColor: Int = 0xFFFFE5A6.toInt()
) : ProceduralTextureParameters

class MagneticFieldTextureGenerator :
    NativeProceduralTextureGenerator<MagneticFieldTextureParameters>(TextureType.MAGNETIC_FIELD) {
    override fun colors(parameters: MagneticFieldTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MagneticFieldTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lineDensity,
        parameters.poleDistortion,
        parameters.lineSharpness,
        parameters.glow,
        parameters.contrast
    )
}
