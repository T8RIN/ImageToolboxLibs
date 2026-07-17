package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChromatophoreTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val cellDensity: Float = 0.68f,
    val pulsePhase: Float = 0.52f,
    val cellSharpness: Float = 0.72f,
    val iridescence: Float = 0.70f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFF160918.toInt(),
    val primaryColor: Int = 0xFF6A275F.toInt(),
    val secondaryColor: Int = 0xFFE2576F.toInt(),
    val highlightColor: Int = 0xFFFFD45E.toInt()
) : ProceduralTextureParameters

class ChromatophoreTextureGenerator :
    NativeProceduralTextureGenerator<ChromatophoreTextureParameters>(TextureType.CHROMATOPHORE) {
    override fun colors(parameters: ChromatophoreTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ChromatophoreTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.cellDensity,
        parameters.pulsePhase,
        parameters.cellSharpness,
        parameters.iridescence,
        parameters.contrast
    )
}
