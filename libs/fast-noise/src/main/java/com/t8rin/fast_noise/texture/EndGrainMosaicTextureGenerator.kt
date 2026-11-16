package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class EndGrainMosaicTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val blockDensity: Float = 0.55f,
    val ringDensity: Float = 0.48f,
    val irregularity: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF25160F.toInt(),
    val primaryColor: Int = 0xFF81512A.toInt(),
    val secondaryColor: Int = 0xFFB88A51.toInt(),
    val highlightColor: Int = 0xFFEAD7A1.toInt()
) : ProceduralTextureParameters

class EndGrainMosaicTextureGenerator :
    NativeProceduralTextureGenerator<EndGrainMosaicTextureParameters>(TextureType.END_GRAIN_MOSAIC) {
    override fun colors(parameters: EndGrainMosaicTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: EndGrainMosaicTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.blockDensity,
        parameters.ringDensity,
        parameters.irregularity,
        parameters.polish,
        parameters.contrast
    )
}
