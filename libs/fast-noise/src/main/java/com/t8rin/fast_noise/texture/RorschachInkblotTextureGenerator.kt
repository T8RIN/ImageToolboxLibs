package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RorschachInkblotTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val lobeCount: Float = 0.70f,
    val symmetry: Float = 0.82f,
    val edgeSharpness: Float = 0.68f,
    val bleed: Float = 0.52f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFFF1EBDD.toInt(),
    val primaryColor: Int = 0xFF8B8590.toInt(),
    val secondaryColor: Int = 0xFF3D283E.toInt(),
    val highlightColor: Int = 0xFF100C14.toInt()
) : ProceduralTextureParameters

class RorschachInkblotTextureGenerator :
    NativeProceduralTextureGenerator<RorschachInkblotTextureParameters>(TextureType.RORSCHACH_INKBLOT) {
    override fun colors(parameters: RorschachInkblotTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: RorschachInkblotTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.lobeCount,
        parameters.symmetry,
        parameters.edgeSharpness,
        parameters.bleed,
        parameters.contrast
    )
}
