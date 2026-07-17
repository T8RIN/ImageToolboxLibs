package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CircuitBoardTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val traceDensity: Float = 0.64f,
    val routing: Float = 0.25f,
    val traceSharpness: Float = 0.80f,
    val emission: Float = 0.72f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF03170F.toInt(),
    val primaryColor: Int = 0xFF075A3B.toInt(),
    val secondaryColor: Int = 0xFFC28B28.toInt(),
    val highlightColor: Int = 0xFF7BFFD1.toInt()
) : ProceduralTextureParameters

class CircuitBoardTextureGenerator :
    NativeProceduralTextureGenerator<CircuitBoardTextureParameters>(TextureType.CIRCUIT_BOARD) {
    override fun colors(parameters: CircuitBoardTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CircuitBoardTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.traceDensity,
        parameters.routing,
        parameters.traceSharpness,
        parameters.emission,
        parameters.contrast
    )
}
