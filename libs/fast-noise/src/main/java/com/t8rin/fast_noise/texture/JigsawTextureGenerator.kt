package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class JigsawTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val tabRadius: Float = 0.6f,
    val irregularity: Float = 0.5f,
    val colorVariation: Float = 0.6f,
    val roughness: Float = 0.4f,
    val bevel: Float = 0.5f,
    val backgroundColor: Int = 0xFF18243A.toInt(),
    val primaryColor: Int = 0xFF4589A4.toInt(),
    val secondaryColor: Int = 0xFFE6AD72.toInt(),
    val highlightColor: Int = 0xFFFFF0CD.toInt()
) : ProceduralTextureParameters

class JigsawTextureGenerator :
    NativeProceduralTextureGenerator<JigsawTextureParameters>(TextureType.JIGSAW) {
    override fun colors(parameters: JigsawTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: JigsawTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tabRadius,
        parameters.irregularity,
        parameters.colorVariation,
        parameters.roughness,
        parameters.bevel
    )
}
