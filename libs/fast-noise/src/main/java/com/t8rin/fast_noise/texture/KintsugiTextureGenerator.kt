package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class KintsugiTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val crackDensity: Float = 0.60f,
    val irregularity: Float = 0.48f,
    val goldSharpness: Float = 0.82f,
    val metallic: Float = 0.70f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF10131A.toInt(),
    val primaryColor: Int = 0xFF344158.toInt(),
    val secondaryColor: Int = 0xFFD99B32.toInt(),
    val highlightColor: Int = 0xFFFFE7A3.toInt()
) : ProceduralTextureParameters

class KintsugiTextureGenerator :
    NativeProceduralTextureGenerator<KintsugiTextureParameters>(TextureType.KINTSUGI) {
    override fun colors(parameters: KintsugiTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: KintsugiTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.crackDensity,
        parameters.irregularity,
        parameters.goldSharpness,
        parameters.metallic,
        parameters.contrast
    )
}
