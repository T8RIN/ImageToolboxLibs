package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PenroseTilingTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val rotation: Float = 0f,
    val jointWidth: Float = 0.22f,
    val roughness: Float = 0.3f,
    val inset: Float = 0.4f,
    val inlayWidth: Float = 0.4f,
    val backgroundColor: Int = 0xFF101E2C.toInt(),
    val primaryColor: Int = 0xFF247F91.toInt(),
    val secondaryColor: Int = 0xFFD7A448.toInt(),
    val highlightColor: Int = 0xFFFFF0C8.toInt()
) : ProceduralTextureParameters

class PenroseTilingTextureGenerator :
    NativeProceduralTextureGenerator<PenroseTilingTextureParameters>(TextureType.PENROSE_TILING) {
    override fun colors(parameters: PenroseTilingTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PenroseTilingTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.jointWidth,
        parameters.roughness,
        parameters.inset,
        parameters.inlayWidth
    )
}
