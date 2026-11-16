package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChainmailTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val ringRadius: Float = 0.65f,
    val wireWidth: Float = 0.45f,
    val flattening: Float = 0.35f,
    val weaveDepth: Float = 0.7f,
    val polish: Float = 0.65f,
    val backgroundColor: Int = 0xFF080E18.toInt(),
    val primaryColor: Int = 0xFF4A6079.toInt(),
    val secondaryColor: Int = 0xFFC89145.toInt(),
    val highlightColor: Int = 0xFFE9F1FC.toInt()
) : ProceduralTextureParameters

class ChainmailTextureGenerator :
    NativeProceduralTextureGenerator<ChainmailTextureParameters>(TextureType.CHAINMAIL) {
    override fun colors(parameters: ChainmailTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ChainmailTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ringRadius,
        parameters.wireWidth,
        parameters.flattening,
        parameters.weaveDepth,
        parameters.polish
    )
}
