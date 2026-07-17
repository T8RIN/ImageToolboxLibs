package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CellularEmbryoTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val embryoDensity: Float = 0.62f,
    val morphology: Float = 0.48f,
    val membraneSharpness: Float = 0.72f,
    val glow: Float = 0.58f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF220E26.toInt(),
    val primaryColor: Int = 0xFF723259.toInt(),
    val secondaryColor: Int = 0xFFE07B93.toInt(),
    val highlightColor: Int = 0xFFFFD0B3.toInt()
) : ProceduralTextureParameters

class CellularEmbryoTextureGenerator :
    NativeProceduralTextureGenerator<CellularEmbryoTextureParameters>(TextureType.CELLULAR_EMBRYO) {
    override fun colors(parameters: CellularEmbryoTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CellularEmbryoTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.embryoDensity,
        parameters.morphology,
        parameters.membraneSharpness,
        parameters.glow,
        parameters.contrast
    )
}
