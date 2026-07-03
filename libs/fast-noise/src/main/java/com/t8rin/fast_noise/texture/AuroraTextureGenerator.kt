package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class AuroraTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.004f,
    val ribbons: Float = 7f,
    val distortion: Float = 34f,
    val glow: Float = 0.72f,
    val stars: Float = 0.2f,
    val contrast: Float = 1.32f,
    val skyColor: Int = 0xFF061227.toInt(),
    val greenColor: Int = 0xFF42D9A0.toInt(),
    val cyanColor: Int = 0xFF65E4E8.toInt(),
    val violetColor: Int = 0xFFA56DE2.toInt()
) : ProceduralTextureParameters

class AuroraTextureGenerator :
    NativeProceduralTextureGenerator<AuroraTextureParameters>(TextureType.AURORA) {
    override fun colors(parameters: AuroraTextureParameters) = intArrayOf(
        parameters.skyColor,
        parameters.greenColor,
        parameters.cyanColor,
        parameters.violetColor
    )

    override fun values(parameters: AuroraTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ribbons,
        parameters.distortion,
        parameters.glow,
        parameters.stars,
        parameters.contrast
    )
}
