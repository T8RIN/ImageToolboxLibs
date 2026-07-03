package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class OilSlickTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bands: Float = 13f,
    val distortion: Float = 32f,
    val iridescence: Float = 0.82f,
    val darkness: Float = 0.3f,
    val contrast: Float = 1.2f,
    val darkColor: Int = 0xFF10101A.toInt(),
    val magentaColor: Int = 0xFFD5329B.toInt(),
    val cyanColor: Int = 0xFF25D4D0.toInt(),
    val goldColor: Int = 0xFFF0C33C.toInt()
) : ProceduralTextureParameters

class OilSlickTextureGenerator :
    NativeProceduralTextureGenerator<OilSlickTextureParameters>(TextureType.OIL_SLICK) {
    override fun colors(parameters: OilSlickTextureParameters) = intArrayOf(
        parameters.darkColor,
        parameters.magentaColor,
        parameters.cyanColor,
        parameters.goldColor
    )

    override fun values(parameters: OilSlickTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bands,
        parameters.distortion,
        parameters.iridescence,
        parameters.darkness,
        parameters.contrast
    )
}
