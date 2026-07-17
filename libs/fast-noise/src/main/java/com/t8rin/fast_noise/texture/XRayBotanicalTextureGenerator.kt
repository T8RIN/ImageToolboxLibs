package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class XRayBotanicalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val leafDensity: Float = 0.66f,
    val curvature: Float = 0.48f,
    val veinSharpness: Float = 0.76f,
    val luminescence: Float = 0.74f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF020A0B.toInt(),
    val primaryColor: Int = 0xFF0C4F50.toInt(),
    val secondaryColor: Int = 0xFF5CC7A7.toInt(),
    val highlightColor: Int = 0xFFE8FFE0.toInt()
) : ProceduralTextureParameters

class XRayBotanicalTextureGenerator :
    NativeProceduralTextureGenerator<XRayBotanicalTextureParameters>(TextureType.X_RAY_BOTANICAL) {
    override fun colors(parameters: XRayBotanicalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: XRayBotanicalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.leafDensity,
        parameters.curvature,
        parameters.veinSharpness,
        parameters.luminescence,
        parameters.contrast
    )
}
