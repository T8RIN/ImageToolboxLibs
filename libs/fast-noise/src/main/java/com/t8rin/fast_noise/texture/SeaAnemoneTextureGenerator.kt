package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SeaAnemoneTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val tentacles: Float = 0.55f,
    val curl: Float = 0.48f,
    val thickness: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF071B30.toInt(),
    val primaryColor: Int = 0xFF286E83.toInt(),
    val secondaryColor: Int = 0xFFCE7DAD.toInt(),
    val highlightColor: Int = 0xFFF2DAB6.toInt()
) : ProceduralTextureParameters

class SeaAnemoneTextureGenerator :
    NativeProceduralTextureGenerator<SeaAnemoneTextureParameters>(TextureType.SEA_ANEMONE) {
    override fun colors(parameters: SeaAnemoneTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SeaAnemoneTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tentacles,
        parameters.curl,
        parameters.thickness,
        parameters.glow,
        parameters.contrast
    )
}
