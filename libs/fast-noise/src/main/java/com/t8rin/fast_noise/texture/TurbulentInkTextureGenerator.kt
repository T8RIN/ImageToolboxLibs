package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TurbulentInkTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val ribbonDensity: Float = 0.70f,
    val turbulence: Float = 0.78f,
    val edgeSharpness: Float = 0.68f,
    val bleed: Float = 0.62f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFFF0E8D8.toInt(),
    val primaryColor: Int = 0xFF174A72.toInt(),
    val secondaryColor: Int = 0xFF9E284F.toInt(),
    val highlightColor: Int = 0xFF171629.toInt()
) : ProceduralTextureParameters

class TurbulentInkTextureGenerator :
    NativeProceduralTextureGenerator<TurbulentInkTextureParameters>(TextureType.TURBULENT_INK) {
    override fun colors(parameters: TurbulentInkTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TurbulentInkTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ribbonDensity,
        parameters.turbulence,
        parameters.edgeSharpness,
        parameters.bleed,
        parameters.contrast
    )
}
