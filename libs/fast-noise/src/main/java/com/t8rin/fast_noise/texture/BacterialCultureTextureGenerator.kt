package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BacterialCultureTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val colonyDensity: Float = 0.68f,
    val growthVariation: Float = 0.62f,
    val membraneSharpness: Float = 0.68f,
    val glow: Float = 0.58f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF160C20.toInt(),
    val primaryColor: Int = 0xFF663078.toInt(),
    val secondaryColor: Int = 0xFFE05C9E.toInt(),
    val highlightColor: Int = 0xFFFFD37C.toInt()
) : ProceduralTextureParameters

class BacterialCultureTextureGenerator :
    NativeProceduralTextureGenerator<BacterialCultureTextureParameters>(TextureType.BACTERIAL_CULTURE) {
    override fun colors(parameters: BacterialCultureTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BacterialCultureTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.colonyDensity,
        parameters.growthVariation,
        parameters.membraneSharpness,
        parameters.glow,
        parameters.contrast
    )
}
