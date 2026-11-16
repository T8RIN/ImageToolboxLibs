package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HoundstoothTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val rotation: Float = 0.5f,
    val threadDensity: Float = 0.5f,
    val weave: Float = 0.55f,
    val fuzz: Float = 0.3f,
    val brightness: Float = 0.25f,
    val backgroundColor: Int = 0xFF111923.toInt(),
    val primaryColor: Int = 0xFF435467.toInt(),
    val secondaryColor: Int = 0xFFD9D3BF.toInt(),
    val highlightColor: Int = 0xFFFFF7DC.toInt()
) : ProceduralTextureParameters

class HoundstoothTextureGenerator :
    NativeProceduralTextureGenerator<HoundstoothTextureParameters>(TextureType.HOUNDSTOOTH) {
    override fun colors(parameters: HoundstoothTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HoundstoothTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.threadDensity,
        parameters.weave,
        parameters.fuzz,
        parameters.brightness
    )
}
