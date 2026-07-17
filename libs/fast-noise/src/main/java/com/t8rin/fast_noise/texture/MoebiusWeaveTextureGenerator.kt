package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MoebiusWeaveTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bandCount: Float = 0.66f,
    val twist: Float = 0.62f,
    val edgeSharpness: Float = 0.76f,
    val sheen: Float = 0.68f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF080A16.toInt(),
    val primaryColor: Int = 0xFF374A94.toInt(),
    val secondaryColor: Int = 0xFFB34EC2.toInt(),
    val highlightColor: Int = 0xFFF5D77C.toInt()
) : ProceduralTextureParameters

class MoebiusWeaveTextureGenerator :
    NativeProceduralTextureGenerator<MoebiusWeaveTextureParameters>(TextureType.MOEBIUS_WEAVE) {
    override fun colors(parameters: MoebiusWeaveTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MoebiusWeaveTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bandCount,
        parameters.twist,
        parameters.edgeSharpness,
        parameters.sheen,
        parameters.contrast
    )
}
