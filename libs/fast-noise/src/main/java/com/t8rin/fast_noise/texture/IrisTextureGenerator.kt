package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class IrisTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val pupilSize: Float = 0.12f,
    val irisSize: Float = 0.38f,
    val fibers: Float = 46f,
    val colorVariation: Float = 0.72f,
    val catchlight: Float = 0.82f,
    val backgroundColor: Int = 0xFF080A0D.toInt(),
    val outerColor: Int = 0xFF123646.toInt(),
    val innerColor: Int = 0xFF37B6A5.toInt(),
    val goldColor: Int = 0xFFE6B650.toInt()
) : ProceduralTextureParameters

class IrisTextureGenerator :
    NativeProceduralTextureGenerator<IrisTextureParameters>(TextureType.IRIS) {
    override fun colors(parameters: IrisTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.outerColor,
        parameters.innerColor,
        parameters.goldColor
    )

    override fun values(parameters: IrisTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.pupilSize,
        parameters.irisSize,
        parameters.fibers,
        parameters.colorVariation,
        parameters.catchlight
    )
}
