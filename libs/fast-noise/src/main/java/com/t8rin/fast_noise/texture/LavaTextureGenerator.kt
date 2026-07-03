package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LavaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val distortion: Float = 32f,
    val flow: Float = 1.35f,
    val detail: Float = 0.55f,
    val crust: Float = 0.52f,
    val glow: Float = 0.72f,
    val crustColor: Int = 0xFF120B0A.toInt(),
    val lavaColor: Int = 0xFFD52B0A.toInt(),
    val glowColor: Int = 0xFFFFD45A.toInt()
) : ProceduralTextureParameters

class LavaTextureGenerator :
    NativeProceduralTextureGenerator<LavaTextureParameters>(TextureType.LAVA) {
    override fun colors(parameters: LavaTextureParameters) = intArrayOf(
        parameters.crustColor,
        parameters.lavaColor,
        parameters.glowColor
    )

    override fun values(parameters: LavaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.distortion,
        parameters.flow,
        parameters.detail,
        parameters.crust,
        parameters.glow
    )
}
