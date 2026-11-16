package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SonicBoomTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val waveCount: Float = 0.55f,
    val machAngle: Float = 0.48f,
    val thickness: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0B2340.toInt(),
    val primaryColor: Int = 0xFF307C9C.toInt(),
    val secondaryColor: Int = 0xFFB67CAD.toInt(),
    val highlightColor: Int = 0xFFF1E0AA.toInt()
) : ProceduralTextureParameters

class SonicBoomTextureGenerator :
    NativeProceduralTextureGenerator<SonicBoomTextureParameters>(TextureType.SONIC_BOOM) {
    override fun colors(parameters: SonicBoomTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SonicBoomTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.waveCount,
        parameters.machAngle,
        parameters.thickness,
        parameters.glow,
        parameters.contrast
    )
}
