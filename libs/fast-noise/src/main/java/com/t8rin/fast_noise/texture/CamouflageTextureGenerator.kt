package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CamouflageTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val firstThreshold: Float = 0.36f,
    val secondThreshold: Float = 0.58f,
    val thirdThreshold: Float = 0.76f,
    val distortion: Float = 28f,
    val edgeSoftness: Float = 0.035f,
    val darkColor: Int = 0xFF202719.toInt(),
    val forestColor: Int = 0xFF4F6134.toInt(),
    val earthColor: Int = 0xFF786344.toInt(),
    val sandColor: Int = 0xFFB4A477.toInt()
) : ProceduralTextureParameters

class CamouflageTextureGenerator :
    NativeProceduralTextureGenerator<CamouflageTextureParameters>(TextureType.CAMOUFLAGE) {
    override fun colors(parameters: CamouflageTextureParameters) = intArrayOf(
        parameters.darkColor,
        parameters.forestColor,
        parameters.earthColor,
        parameters.sandColor
    )

    override fun values(parameters: CamouflageTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.firstThreshold,
        parameters.secondThreshold,
        parameters.thirdThreshold,
        parameters.distortion,
        parameters.edgeSoftness
    )
}
