package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FractalBloomTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val petals: Float = 7f,
    val layers: Float = 5f,
    val curl: Float = 4.2f,
    val filigree: Float = 0.7f,
    val glow: Float = 0.72f,
    val backgroundColor: Int = 0xFF09061B.toInt(),
    val outerColor: Int = 0xFF5940D6.toInt(),
    val innerColor: Int = 0xFFFF3E93.toInt(),
    val coreColor: Int = 0xFFFFE88A.toInt()
) : ProceduralTextureParameters

class FractalBloomTextureGenerator :
    NativeProceduralTextureGenerator<FractalBloomTextureParameters>(TextureType.FRACTAL_BLOOM) {
    override fun colors(parameters: FractalBloomTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.outerColor,
        parameters.innerColor,
        parameters.coreColor
    )

    override fun values(parameters: FractalBloomTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.petals,
        parameters.layers,
        parameters.curl,
        parameters.filigree,
        parameters.glow
    )
}
