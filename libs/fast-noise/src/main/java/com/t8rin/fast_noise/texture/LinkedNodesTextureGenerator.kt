package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LinkedNodesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val neuronDensity: Float = 0.76f,
    val branching: Float = 0.82f,
    val fiberSharpness: Float = 0.84f,
    val glow: Float = 0.64f,
    val contrast: Float = 1.65f,
    val backgroundColor: Int = 0xFF06120D.toInt(),
    val primaryColor: Int = 0xFF11684A.toInt(),
    val secondaryColor: Int = 0xFF6BD87A.toInt(),
    val highlightColor: Int = 0xFFB8FFBA.toInt()
) : ProceduralTextureParameters

class LinkedNodesTextureGenerator :
    NativeProceduralTextureGenerator<LinkedNodesTextureParameters>(TextureType.LINKED_NODES) {
    override fun colors(parameters: LinkedNodesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LinkedNodesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.neuronDensity,
        parameters.branching,
        parameters.fiberSharpness,
        parameters.glow,
        parameters.contrast
    )
}
