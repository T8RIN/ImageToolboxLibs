package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class IkatTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val motifDensity: Float = 0.55f,
    val chevronHeight: Float = 0.48f,
    val feathering: Float = 0.55f,
    val weave: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF1C2641.toInt(),
    val primaryColor: Int = 0xFF546A9E.toInt(),
    val secondaryColor: Int = 0xFFCC7D68.toInt(),
    val highlightColor: Int = 0xFFF1DAB7.toInt()
) : ProceduralTextureParameters

class IkatTextureGenerator :
    NativeProceduralTextureGenerator<IkatTextureParameters>(TextureType.IKAT) {
    override fun colors(parameters: IkatTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: IkatTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.motifDensity,
        parameters.chevronHeight,
        parameters.feathering,
        parameters.weave,
        parameters.contrast
    )
}
