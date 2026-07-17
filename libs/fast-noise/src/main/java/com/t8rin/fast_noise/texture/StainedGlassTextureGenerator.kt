package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class StainedGlassTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val cells: Float = 0.62f,
    val irregularity: Float = 0.38f,
    val leadSharpness: Float = 0.75f,
    val glow: Float = 0.52f,
    val contrast: Float = 1.25f,
    val backgroundColor: Int = 0xFF120F18.toInt(),
    val primaryColor: Int = 0xFF2E8BD2.toInt(),
    val secondaryColor: Int = 0xFFE54472.toInt(),
    val highlightColor: Int = 0xFFFFD34E.toInt()
) : ProceduralTextureParameters

class StainedGlassTextureGenerator :
    NativeProceduralTextureGenerator<StainedGlassTextureParameters>(TextureType.STAINED_GLASS) {
    override fun colors(parameters: StainedGlassTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: StainedGlassTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.cells,
        parameters.irregularity,
        parameters.leadSharpness,
        parameters.glow,
        parameters.contrast
    )
}
