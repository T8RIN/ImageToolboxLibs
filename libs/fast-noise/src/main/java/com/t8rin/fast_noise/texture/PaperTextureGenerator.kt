package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PaperTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val fiberDensity: Float = 72f,
    val fiberStrength: Float = 0.24f,
    val grain: Float = 0.16f,
    val stains: Float = 0.14f,
    val roughness: Float = 0.35f,
    val baseColor: Int = 0xFFE4D4AE.toInt(),
    val lightColor: Int = 0xFFF5E9C9.toInt(),
    val fiberColor: Int = 0xFFB59C70.toInt(),
    val stainColor: Int = 0xFF8A6841.toInt()
) : ProceduralTextureParameters

class PaperTextureGenerator :
    NativeProceduralTextureGenerator<PaperTextureParameters>(TextureType.PAPER) {
    override fun colors(parameters: PaperTextureParameters) = intArrayOf(
        parameters.baseColor,
        parameters.lightColor,
        parameters.fiberColor,
        parameters.stainColor
    )

    override fun values(parameters: PaperTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.fiberDensity,
        parameters.fiberStrength,
        parameters.grain,
        parameters.stains,
        parameters.roughness
    )
}
