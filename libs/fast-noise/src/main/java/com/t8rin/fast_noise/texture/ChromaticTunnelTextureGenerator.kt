package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChromaticTunnelTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val depth: Float = 18f,
    val twist: Float = 5.5f,
    val facets: Float = 7f,
    val curvature: Float = 0.48f,
    val glow: Float = 0.78f,
    val deepColor: Int = 0xFF030516.toInt(),
    val cyanColor: Int = 0xFF00D5FF.toInt(),
    val magentaColor: Int = 0xFFFF2C9C.toInt(),
    val lightColor: Int = 0xFFFFF1B8.toInt()
) : ProceduralTextureParameters

class ChromaticTunnelTextureGenerator :
    NativeProceduralTextureGenerator<ChromaticTunnelTextureParameters>(TextureType.CHROMATIC_TUNNEL) {
    override fun colors(parameters: ChromaticTunnelTextureParameters) = intArrayOf(
        parameters.deepColor,
        parameters.cyanColor,
        parameters.magentaColor,
        parameters.lightColor
    )

    override fun values(parameters: ChromaticTunnelTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.depth,
        parameters.twist,
        parameters.facets,
        parameters.curvature,
        parameters.glow
    )
}
