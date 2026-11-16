package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class IslamicStarTilingTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val tileRadius: Float = 0.6f,
    val lineWidth: Float = 0.5f,
    val starRadius: Float = 0.45f,
    val roughness: Float = 0.3f,
    val brightness: Float = 0.35f,
    val backgroundColor: Int = 0xFF0C2336.toInt(),
    val primaryColor: Int = 0xFF146F88.toInt(),
    val secondaryColor: Int = 0xFFC0964A.toInt(),
    val highlightColor: Int = 0xFFFFF0C8.toInt()
) : ProceduralTextureParameters

class IslamicStarTilingTextureGenerator :
    NativeProceduralTextureGenerator<IslamicStarTilingTextureParameters>(TextureType.ISLAMIC_STAR_TILING) {
    override fun colors(parameters: IslamicStarTilingTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: IslamicStarTilingTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tileRadius,
        parameters.lineWidth,
        parameters.starRadius,
        parameters.roughness,
        parameters.brightness
    )
}
