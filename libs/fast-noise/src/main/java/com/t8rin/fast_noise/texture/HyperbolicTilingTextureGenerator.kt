package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HyperbolicTilingTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val polygonSides: Float = 0.62f,
    val curvature: Float = 0.72f,
    val edgeSharpness: Float = 0.80f,
    val glow: Float = 0.74f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF050A14.toInt(),
    val primaryColor: Int = 0xFF2454A0.toInt(),
    val secondaryColor: Int = 0xFF30B9B0.toInt(),
    val highlightColor: Int = 0xFFFFD86A.toInt()
) : ProceduralTextureParameters

class HyperbolicTilingTextureGenerator :
    NativeProceduralTextureGenerator<HyperbolicTilingTextureParameters>(TextureType.HYPERBOLIC_TILING) {
    override fun colors(parameters: HyperbolicTilingTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HyperbolicTilingTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.polygonSides,
        parameters.curvature,
        parameters.edgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
