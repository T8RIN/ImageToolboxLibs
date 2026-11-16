package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class AsanohaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val rotation: Float = 0f,
    val lineWidth: Float = 0.3f,
    val backgroundBlend: Float = 0.4f,
    val roughness: Float = 0.25f,
    val brightness: Float = 0.2f,
    val backgroundColor: Int = 0xFF23182D.toInt(),
    val primaryColor: Int = 0xFF5F3969.toInt(),
    val secondaryColor: Int = 0xFFCB8F55.toInt(),
    val highlightColor: Int = 0xFFFFE7BD.toInt()
) : ProceduralTextureParameters

class AsanohaTextureGenerator :
    NativeProceduralTextureGenerator<AsanohaTextureParameters>(TextureType.ASANOHA) {
    override fun colors(parameters: AsanohaTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: AsanohaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rotation,
        parameters.lineWidth,
        parameters.backgroundBlend,
        parameters.roughness,
        parameters.brightness
    )
}
