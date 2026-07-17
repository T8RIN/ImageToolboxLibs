package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CoralGrowthTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branchDensity: Float = 0.68f,
    val growthBias: Float = 0.62f,
    val edgeSharpness: Float = 0.70f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFF071D2A.toInt(),
    val primaryColor: Int = 0xFFEA536E.toInt(),
    val secondaryColor: Int = 0xFFFFA468.toInt(),
    val highlightColor: Int = 0xFFFFE2B8.toInt()
) : ProceduralTextureParameters

class CoralGrowthTextureGenerator :
    NativeProceduralTextureGenerator<CoralGrowthTextureParameters>(TextureType.CORAL_GROWTH) {
    override fun colors(parameters: CoralGrowthTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CoralGrowthTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branchDensity,
        parameters.growthBias,
        parameters.edgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
