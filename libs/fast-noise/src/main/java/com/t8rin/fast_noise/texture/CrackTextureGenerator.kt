package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CrackTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.019f,
    val width: Float = 0.065f,
    val density: Float = 0.72f,
    val distortion: Float = 9f,
    val depth: Float = 0.72f,
    val branching: Float = 0.45f,
    val surfaceColor: Int = 0xFF8D8374.toInt(),
    val variationColor: Int = 0xFFC0B6A5.toInt(),
    val crackColor: Int = 0xFF161412.toInt(),
    val edgeColor: Int = 0xFF554E45.toInt()
) : ProceduralTextureParameters

class CrackTextureGenerator :
    NativeProceduralTextureGenerator<CrackTextureParameters>(TextureType.CRACKS) {
    override fun colors(parameters: CrackTextureParameters) = intArrayOf(
        parameters.surfaceColor,
        parameters.variationColor,
        parameters.crackColor,
        parameters.edgeColor
    )

    override fun values(parameters: CrackTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.width,
        parameters.density,
        parameters.distortion,
        parameters.depth,
        parameters.branching
    )
}
