package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ZebraStripesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.01f,
    val angle: Float = 0.5f,
    val distortion: Float = 0.55f,
    val frequency: Float = 0.4f,
    val branching: Float = 0.45f,
    val balance: Float = 0.5f,
    val backgroundColor: Int = 0xFF111721.toInt(),
    val primaryColor: Int = 0xFF3F4851.toInt(),
    val secondaryColor: Int = 0xFFDBDCCB.toInt(),
    val highlightColor: Int = 0xFFFFF9E7.toInt()
) : ProceduralTextureParameters

class ZebraStripesTextureGenerator :
    NativeProceduralTextureGenerator<ZebraStripesTextureParameters>(TextureType.ZEBRA_STRIPES) {
    override fun colors(parameters: ZebraStripesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ZebraStripesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.angle,
        parameters.distortion,
        parameters.frequency,
        parameters.branching,
        parameters.balance
    )
}
