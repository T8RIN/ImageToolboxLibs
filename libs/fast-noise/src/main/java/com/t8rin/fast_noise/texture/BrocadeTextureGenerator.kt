package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BrocadeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val ornamentDensity: Float = 0.55f,
    val curvature: Float = 0.48f,
    val threadWidth: Float = 0.55f,
    val metallic: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF271425.toInt(),
    val primaryColor: Int = 0xFF74404C.toInt(),
    val secondaryColor: Int = 0xFFCAA45F.toInt(),
    val highlightColor: Int = 0xFFF8DE99.toInt()
) : ProceduralTextureParameters

class BrocadeTextureGenerator :
    NativeProceduralTextureGenerator<BrocadeTextureParameters>(TextureType.BROCADE) {
    override fun colors(parameters: BrocadeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BrocadeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ornamentDensity,
        parameters.curvature,
        parameters.threadWidth,
        parameters.metallic,
        parameters.contrast
    )
}
