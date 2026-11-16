package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MalachiteTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val bandDensity: Float = 0.55f,
    val warping: Float = 0.48f,
    val relief: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF071F18.toInt(),
    val primaryColor: Int = 0xFF17613E.toInt(),
    val secondaryColor: Int = 0xFF56AE70.toInt(),
    val highlightColor: Int = 0xFFD5F0B4.toInt()
) : ProceduralTextureParameters

class MalachiteTextureGenerator :
    NativeProceduralTextureGenerator<MalachiteTextureParameters>(TextureType.MALACHITE) {
    override fun colors(parameters: MalachiteTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MalachiteTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bandDensity,
        parameters.warping,
        parameters.relief,
        parameters.polish,
        parameters.contrast
    )
}
