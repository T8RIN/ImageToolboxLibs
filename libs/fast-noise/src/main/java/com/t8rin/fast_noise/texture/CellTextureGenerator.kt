package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CellTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val jitter: Float = 0.92f,
    val borderWidth: Float = 0.12f,
    val glow: Float = 0.28f,
    val distortion: Float = 6f,
    val variation: Float = 0.42f,
    val backgroundColor: Int = 0xFF101A24.toInt(),
    val cellColor: Int = 0xFF216E78.toInt(),
    val edgeColor: Int = 0xFF5AD1C8.toInt(),
    val highlightColor: Int = 0xFFC9FFF4.toInt()
) : ProceduralTextureParameters

class CellTextureGenerator :
    NativeProceduralTextureGenerator<CellTextureParameters>(TextureType.CELLS) {
    override fun colors(parameters: CellTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.cellColor,
        parameters.edgeColor,
        parameters.highlightColor
    )

    override fun values(parameters: CellTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.jitter,
        parameters.borderWidth,
        parameters.glow,
        parameters.distortion,
        parameters.variation
    )
}
