package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class NeuralGardenTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val neuronDensity: Float = 0.70f,
    val branching: Float = 0.72f,
    val fiberSharpness: Float = 0.78f,
    val glow: Float = 0.78f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF06120D.toInt(),
    val primaryColor: Int = 0xFF11684A.toInt(),
    val secondaryColor: Int = 0xFF6BD87A.toInt(),
    val highlightColor: Int = 0xFFE7F58E.toInt()
) : ProceduralTextureParameters

class NeuralGardenTextureGenerator :
    NativeProceduralTextureGenerator<NeuralGardenTextureParameters>(TextureType.NEURAL_GARDEN) {
    override fun colors(parameters: NeuralGardenTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: NeuralGardenTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.neuronDensity,
        parameters.branching,
        parameters.fiberSharpness,
        parameters.glow,
        parameters.contrast
    )
}
