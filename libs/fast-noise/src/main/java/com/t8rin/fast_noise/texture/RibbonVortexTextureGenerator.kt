package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RibbonVortexTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val ribbonCount: Float = 0.55f,
    val twist: Float = 0.48f,
    val thickness: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF0C1C34.toInt(),
    val primaryColor: Int = 0xFF368EA4.toInt(),
    val secondaryColor: Int = 0xFFD47D9F.toInt(),
    val highlightColor: Int = 0xFFEFD397.toInt()
) : ProceduralTextureParameters

class RibbonVortexTextureGenerator :
    NativeProceduralTextureGenerator<RibbonVortexTextureParameters>(TextureType.RIBBON_VORTEX) {
    override fun colors(parameters: RibbonVortexTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RibbonVortexTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ribbonCount,
        parameters.twist,
        parameters.thickness,
        parameters.light,
        parameters.contrast
    )
}
