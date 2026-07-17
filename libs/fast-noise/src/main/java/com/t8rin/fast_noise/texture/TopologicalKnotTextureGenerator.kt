package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TopologicalKnotTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val turns: Float = 0.62f,
    val twist: Float = 0.54f,
    val tubeSharpness: Float = 0.82f,
    val glow: Float = 0.76f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF040713.toInt(),
    val primaryColor: Int = 0xFF24579A.toInt(),
    val secondaryColor: Int = 0xFFC245D6.toInt(),
    val highlightColor: Int = 0xFFFFE29A.toInt()
) : ProceduralTextureParameters

class TopologicalKnotTextureGenerator :
    NativeProceduralTextureGenerator<TopologicalKnotTextureParameters>(TextureType.TOPOLOGICAL_KNOT) {
    override fun colors(parameters: TopologicalKnotTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TopologicalKnotTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.turns,
        parameters.twist,
        parameters.tubeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
