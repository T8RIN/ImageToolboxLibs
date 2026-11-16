package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class PulsarJetsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val jetWidth: Float = 0.55f,
    val precession: Float = 0.48f,
    val shockRings: Float = 0.55f,
    val glow: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF07142B.toInt(),
    val primaryColor: Int = 0xFF336CB0.toInt(),
    val secondaryColor: Int = 0xFFC078AD.toInt(),
    val highlightColor: Int = 0xFFF2E5B4.toInt()
) : ProceduralTextureParameters

class PulsarJetsTextureGenerator :
    NativeProceduralTextureGenerator<PulsarJetsTextureParameters>(TextureType.PULSAR_JETS) {
    override fun colors(parameters: PulsarJetsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: PulsarJetsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.jetWidth,
        parameters.precession,
        parameters.shockRings,
        parameters.glow,
        parameters.contrast
    )
}
