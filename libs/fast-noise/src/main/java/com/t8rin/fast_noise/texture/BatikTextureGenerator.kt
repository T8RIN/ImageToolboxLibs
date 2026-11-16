package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BatikTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val flowerDensity: Float = 0.55f,
    val petals: Float = 0.48f,
    val waxWidth: Float = 0.55f,
    val distortion: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF102A3B.toInt(),
    val primaryColor: Int = 0xFF397D85.toInt(),
    val secondaryColor: Int = 0xFF9EB68B.toInt(),
    val highlightColor: Int = 0xFFF4D69E.toInt()
) : ProceduralTextureParameters

class BatikTextureGenerator :
    NativeProceduralTextureGenerator<BatikTextureParameters>(TextureType.BATIK) {
    override fun colors(parameters: BatikTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BatikTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.flowerDensity,
        parameters.petals,
        parameters.waxWidth,
        parameters.distortion,
        parameters.contrast
    )
}
