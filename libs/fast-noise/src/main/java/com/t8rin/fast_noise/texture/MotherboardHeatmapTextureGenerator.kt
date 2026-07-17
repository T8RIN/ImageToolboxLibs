package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MotherboardHeatmapTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val traceDensity: Float = 0.62f,
    val heatSpread: Float = 0.38f,
    val traceSharpness: Float = 0.76f,
    val heatGlow: Float = 0.68f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF07120E.toInt(),
    val primaryColor: Int = 0xFF08705B.toInt(),
    val secondaryColor: Int = 0xFFFF7B22.toInt(),
    val highlightColor: Int = 0xFFFFF1A8.toInt()
) : ProceduralTextureParameters

class MotherboardHeatmapTextureGenerator :
    NativeProceduralTextureGenerator<MotherboardHeatmapTextureParameters>(TextureType.MOTHERBOARD_HEATMAP) {
    override fun colors(parameters: MotherboardHeatmapTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MotherboardHeatmapTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.traceDensity,
        parameters.heatSpread,
        parameters.traceSharpness,
        parameters.heatGlow,
        parameters.contrast
    )
}
