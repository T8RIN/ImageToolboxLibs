package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RoofTilesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.02f,
    val curvature: Float = 0.6f,
    val roughness: Float = 0.25f,
    val relief: Float = 0.8f,
    val shadows: Float = 0.5f,
    val variation: Float = 0.6f,
    val backgroundColor: Int = 0xFF32150F.toInt(),
    val primaryColor: Int = 0xFFA34125.toInt(),
    val secondaryColor: Int = 0xFFD57A43.toInt(),
    val highlightColor: Int = 0xFFF5BD87.toInt()
) : ProceduralTextureParameters

class RoofTilesTextureGenerator :
    NativeProceduralTextureGenerator<RoofTilesTextureParameters>(TextureType.ROOF_TILES) {
    override fun colors(parameters: RoofTilesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RoofTilesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.curvature,
        parameters.roughness,
        parameters.relief,
        parameters.shadows,
        parameters.variation
    )
}
