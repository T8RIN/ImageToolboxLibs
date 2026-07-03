package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PeacockFeatherTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val eyeSize: Float = 0.24f,
    val barbDensity: Float = 54f,
    val curvature: Float = 0.58f,
    val iridescence: Float = 0.8f,
    val softness: Float = 0.42f,
    val backgroundColor: Int = 0xFF06150F.toInt(),
    val featherColor: Int = 0xFF167D55.toInt(),
    val blueColor: Int = 0xFF1248BC.toInt(),
    val goldColor: Int = 0xFFE4BC45.toInt()
) : ProceduralTextureParameters

class PeacockFeatherTextureGenerator :
    NativeProceduralTextureGenerator<PeacockFeatherTextureParameters>(TextureType.PEACOCK_FEATHER) {
    override fun colors(parameters: PeacockFeatherTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.featherColor,
        parameters.blueColor,
        parameters.goldColor
    )

    override fun values(parameters: PeacockFeatherTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.eyeSize,
        parameters.barbDensity,
        parameters.curvature,
        parameters.iridescence,
        parameters.softness
    )
}
