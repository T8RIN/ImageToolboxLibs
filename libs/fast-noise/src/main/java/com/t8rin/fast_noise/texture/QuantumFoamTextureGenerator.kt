package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class QuantumFoamTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bubbleDensity: Float = 0.72f,
    val uncertainty: Float = 0.78f,
    val boundarySharpness: Float = 0.72f,
    val glow: Float = 0.78f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF060616.toInt(),
    val primaryColor: Int = 0xFF353499.toInt(),
    val secondaryColor: Int = 0xFF23C9CB.toInt(),
    val highlightColor: Int = 0xFFF66AC2.toInt()
) : ProceduralTextureParameters

class QuantumFoamTextureGenerator :
    NativeProceduralTextureGenerator<QuantumFoamTextureParameters>(TextureType.QUANTUM_FOAM) {
    override fun colors(parameters: QuantumFoamTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: QuantumFoamTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bubbleDensity,
        parameters.uncertainty,
        parameters.boundarySharpness,
        parameters.glow,
        parameters.contrast
    )
}
