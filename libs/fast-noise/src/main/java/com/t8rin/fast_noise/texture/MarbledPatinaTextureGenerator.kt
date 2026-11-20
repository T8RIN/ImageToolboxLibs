package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MarbledPatinaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val patternScale: Float = 0.82f,
    val feedback: Float = 0.74f,
    val edgeSharpness: Float = 0.8f,
    val glow: Float = 0.5f,
    val contrast: Float = 1.6f,
    val backgroundColor: Int = 0xFF090B17.toInt(),
    val primaryColor: Int = 0xFF303785.toInt(),
    val secondaryColor: Int = 0xFFE64C93.toInt(),
    val highlightColor: Int = 0xFF59EBB0.toInt()
) : ProceduralTextureParameters

class MarbledPatinaTextureGenerator :
    NativeProceduralTextureGenerator<MarbledPatinaTextureParameters>(TextureType.MARBLED_PATINA) {
    override fun colors(parameters: MarbledPatinaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MarbledPatinaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.patternScale,
        parameters.feedback,
        parameters.edgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
