package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SlimeMoldTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val trailDensity: Float = 0.72f,
    val chemotaxis: Float = 0.72f,
    val trailSharpness: Float = 0.76f,
    val glow: Float = 0.74f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF08110A.toInt(),
    val primaryColor: Int = 0xFF596F17.toInt(),
    val secondaryColor: Int = 0xFFD5D928.toInt(),
    val highlightColor: Int = 0xFFFFF5A0.toInt()
) : ProceduralTextureParameters

class SlimeMoldTextureGenerator :
    NativeProceduralTextureGenerator<SlimeMoldTextureParameters>(TextureType.SLIME_MOLD) {
    override fun colors(parameters: SlimeMoldTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SlimeMoldTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.trailDensity,
        parameters.chemotaxis,
        parameters.trailSharpness,
        parameters.glow,
        parameters.contrast
    )
}
