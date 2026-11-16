package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LeopardSpotsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.013f,
    val jitter: Float = 0.7f,
    val spotRadius: Float = 0.65f,
    val irregularity: Float = 0.4f,
    val brokenRings: Float = 0.45f,
    val fur: Float = 0.5f,
    val backgroundColor: Int = 0xFF211A16.toInt(),
    val primaryColor: Int = 0xFFC99344.toInt(),
    val secondaryColor: Int = 0xFF8E5528.toInt(),
    val highlightColor: Int = 0xFFF4DB93.toInt()
) : ProceduralTextureParameters

class LeopardSpotsTextureGenerator :
    NativeProceduralTextureGenerator<LeopardSpotsTextureParameters>(TextureType.LEOPARD_SPOTS) {
    override fun colors(parameters: LeopardSpotsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LeopardSpotsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.jitter,
        parameters.spotRadius,
        parameters.irregularity,
        parameters.brokenRings,
        parameters.fur
    )
}
