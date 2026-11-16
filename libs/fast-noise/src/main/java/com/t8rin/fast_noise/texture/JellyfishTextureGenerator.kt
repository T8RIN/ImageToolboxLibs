package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class JellyfishTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val tentacles: Float = 0.55f,
    val bellShape: Float = 0.48f,
    val thickness: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF051C32.toInt(),
    val primaryColor: Int = 0xFF2D7390.toInt(),
    val secondaryColor: Int = 0xFFA98AD6.toInt(),
    val highlightColor: Int = 0xFFDDFAEC.toInt()
) : ProceduralTextureParameters

class JellyfishTextureGenerator :
    NativeProceduralTextureGenerator<JellyfishTextureParameters>(TextureType.JELLYFISH) {
    override fun colors(parameters: JellyfishTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: JellyfishTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tentacles,
        parameters.bellShape,
        parameters.thickness,
        parameters.glow,
        parameters.contrast
    )
}
