package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class AncientRunesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val glyphDensity: Float = 0.62f,
    val erosion: Float = 0.54f,
    val strokeSharpness: Float = 0.78f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF0B0C12.toInt(),
    val primaryColor: Int = 0xFF34364A.toInt(),
    val secondaryColor: Int = 0xFFA85F35.toInt(),
    val highlightColor: Int = 0xFFFFD27A.toInt()
) : ProceduralTextureParameters

class AncientRunesTextureGenerator :
    NativeProceduralTextureGenerator<AncientRunesTextureParameters>(TextureType.ANCIENT_RUNES) {
    override fun colors(parameters: AncientRunesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: AncientRunesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.glyphDensity,
        parameters.erosion,
        parameters.strokeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
