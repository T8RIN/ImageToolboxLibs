package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class WickerWeaveTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val stripWidth: Float = 0.8f,
    val fibers: Float = 0.6f,
    val roughness: Float = 0.3f,
    val angle: Float = 0.5f,
    val variation: Float = 0.5f,
    val backgroundColor: Int = 0xFF271B13.toInt(),
    val primaryColor: Int = 0xFF936435.toInt(),
    val secondaryColor: Int = 0xFFC79954.toInt(),
    val highlightColor: Int = 0xFFF3DFAB.toInt()
) : ProceduralTextureParameters

class WickerWeaveTextureGenerator :
    NativeProceduralTextureGenerator<WickerWeaveTextureParameters>(TextureType.WICKER_WEAVE) {
    override fun colors(parameters: WickerWeaveTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: WickerWeaveTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.stripWidth,
        parameters.fibers,
        parameters.roughness,
        parameters.angle,
        parameters.variation
    )
}
