package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class VolcanicObsidianTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val facets: Float = 0.64f,
    val fracturing: Float = 0.48f,
    val edgeSharpness: Float = 0.75f,
    val lavaGlow: Float = 0.64f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF020304.toInt(),
    val primaryColor: Int = 0xFF17141D.toInt(),
    val secondaryColor: Int = 0xFF3B2744.toInt(),
    val highlightColor: Int = 0xFFEF6C3D.toInt()
) : ProceduralTextureParameters

class VolcanicObsidianTextureGenerator :
    NativeProceduralTextureGenerator<VolcanicObsidianTextureParameters>(TextureType.VOLCANIC_OBSIDIAN) {
    override fun colors(parameters: VolcanicObsidianTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: VolcanicObsidianTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.facets,
        parameters.fracturing,
        parameters.edgeSharpness,
        parameters.lavaGlow,
        parameters.contrast
    )
}
