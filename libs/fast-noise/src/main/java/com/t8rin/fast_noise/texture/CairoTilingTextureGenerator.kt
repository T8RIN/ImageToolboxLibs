package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CairoTilingTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.026f,
    val rotation: Float = 0f,
    val jointWidth: Float = 0.25f,
    val variation: Float = 0.75f,
    val roughness: Float = 0.3f,
    val inlayWidth: Float = 0.25f,
    val backgroundColor: Int = 0xFF172637.toInt(),
    val primaryColor: Int = 0xFF45778A.toInt(),
    val secondaryColor: Int = 0xFFE3AE73.toInt(),
    val highlightColor: Int = 0xFFFFF1D6.toInt()
) : ProceduralTextureParameters

class CairoTilingTextureGenerator :
    NativeProceduralTextureGenerator<CairoTilingTextureParameters>(TextureType.CAIRO_TILING) {
    override fun colors(parameters: CairoTilingTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CairoTilingTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.jointWidth,
        parameters.variation,
        parameters.roughness,
        parameters.inlayWidth
    )
}
