package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HypercubeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val projection: Float = 0.55f,
    val rotation: Float = 0.48f,
    val lineWidth: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF081B30.toInt(),
    val primaryColor: Int = 0xFF2C8AA4.toInt(),
    val secondaryColor: Int = 0xFFD08BAB.toInt(),
    val highlightColor: Int = 0xFFF1D797.toInt()
) : ProceduralTextureParameters

class HypercubeTextureGenerator :
    NativeProceduralTextureGenerator<HypercubeTextureParameters>(TextureType.HYPERCUBE) {
    override fun colors(parameters: HypercubeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HypercubeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.projection,
        parameters.rotation,
        parameters.lineWidth,
        parameters.glow,
        parameters.contrast
    )
}
