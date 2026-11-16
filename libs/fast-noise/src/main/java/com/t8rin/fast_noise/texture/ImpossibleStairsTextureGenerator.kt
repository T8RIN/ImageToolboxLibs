package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ImpossibleStairsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val stairCount: Float = 0.55f,
    val rotation: Float = 0.48f,
    val stepHeight: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF152B3B.toInt(),
    val primaryColor: Int = 0xFF507E91.toInt(),
    val secondaryColor: Int = 0xFFC08C79.toInt(),
    val highlightColor: Int = 0xFFEADAB6.toInt()
) : ProceduralTextureParameters

class ImpossibleStairsTextureGenerator :
    NativeProceduralTextureGenerator<ImpossibleStairsTextureParameters>(TextureType.IMPOSSIBLE_STAIRS) {
    override fun colors(parameters: ImpossibleStairsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ImpossibleStairsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.stairCount,
        parameters.rotation,
        parameters.stepHeight,
        parameters.light,
        parameters.contrast
    )
}
