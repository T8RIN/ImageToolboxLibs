package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PineConeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val scaleLength: Float = 0.7f,
    val relief: Float = 0.7f,
    val creases: Float = 0.35f,
    val variation: Float = 0.45f,
    val edgeWidth: Float = 0.4f,
    val backgroundColor: Int = 0xFF24150E.toInt(),
    val primaryColor: Int = 0xFF865130.toInt(),
    val secondaryColor: Int = 0xFFB78547.toInt(),
    val highlightColor: Int = 0xFFE8C994.toInt()
) : ProceduralTextureParameters

class PineConeTextureGenerator :
    NativeProceduralTextureGenerator<PineConeTextureParameters>(TextureType.PINE_CONE) {
    override fun colors(parameters: PineConeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PineConeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.scaleLength,
        parameters.relief,
        parameters.creases,
        parameters.variation,
        parameters.edgeWidth
    )
}
