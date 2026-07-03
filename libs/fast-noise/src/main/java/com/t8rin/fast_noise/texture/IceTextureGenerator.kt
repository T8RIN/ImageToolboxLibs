package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class IceTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val crackWidth: Float = 0.075f,
    val frost: Float = 0.48f,
    val depth: Float = 0.64f,
    val distortion: Float = 8f,
    val sparkle: Float = 0.32f,
    val deepColor: Int = 0xFF155A79.toInt(),
    val iceColor: Int = 0xFF79C8DC.toInt(),
    val frostColor: Int = 0xFFD8F4F5.toInt(),
    val crackColor: Int = 0xFFF5FFFF.toInt()
) : ProceduralTextureParameters

class IceTextureGenerator :
    NativeProceduralTextureGenerator<IceTextureParameters>(TextureType.ICE) {
    override fun colors(parameters: IceTextureParameters) = intArrayOf(
        parameters.deepColor,
        parameters.iceColor,
        parameters.frostColor,
        parameters.crackColor
    )

    override fun values(parameters: IceTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.crackWidth,
        parameters.frost,
        parameters.depth,
        parameters.distortion,
        parameters.sparkle
    )
}
