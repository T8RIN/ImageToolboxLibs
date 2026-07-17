package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LichtenbergFigureTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branchCount: Float = 0.72f,
    val branching: Float = 0.66f,
    val arcSharpness: Float = 0.84f,
    val glow: Float = 0.90f,
    val contrast: Float = 1.60f,
    val backgroundColor: Int = 0xFF08030C.toInt(),
    val primaryColor: Int = 0xFF5E176E.toInt(),
    val secondaryColor: Int = 0xFFE544A7.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class LichtenbergFigureTextureGenerator :
    NativeProceduralTextureGenerator<LichtenbergFigureTextureParameters>(TextureType.LICHTENBERG_FIGURE) {
    override fun colors(parameters: LichtenbergFigureTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: LichtenbergFigureTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branchCount,
        parameters.branching,
        parameters.arcSharpness,
        parameters.glow,
        parameters.contrast
    )
}
