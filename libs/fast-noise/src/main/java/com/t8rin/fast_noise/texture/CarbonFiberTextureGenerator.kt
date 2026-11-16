package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CarbonFiberTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.010f,
    val weaveDensity: Float = 0.72f,
    val twill: Float = 0.3f,
    val fiberSharpness: Float = 0.82f,
    val sheen: Float = 0.34f,
    val contrast: Float = 1.65f,
    val backgroundColor: Int = 0xFF030405.toInt(),
    val primaryColor: Int = 0xFF15191C.toInt(),
    val secondaryColor: Int = 0xFF343B40.toInt(),
    val highlightColor: Int = 0xFF88949B.toInt()
) : ProceduralTextureParameters

class CarbonFiberTextureGenerator :
    NativeProceduralTextureGenerator<CarbonFiberTextureParameters>(TextureType.CARBON_FIBER) {
    override fun colors(parameters: CarbonFiberTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CarbonFiberTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.weaveDensity,
        parameters.twill,
        parameters.fiberSharpness,
        parameters.sheen,
        parameters.contrast
    )
}
