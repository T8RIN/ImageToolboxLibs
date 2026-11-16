package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FungalGillsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val gillDensity: Float = 0.55f,
    val undulation: Float = 0.48f,
    val relief: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF241B29.toInt(),
    val primaryColor: Int = 0xFF785C72.toInt(),
    val secondaryColor: Int = 0xFFBD9A8B.toInt(),
    val highlightColor: Int = 0xFFEFDABD.toInt()
) : ProceduralTextureParameters

class FungalGillsTextureGenerator :
    NativeProceduralTextureGenerator<FungalGillsTextureParameters>(TextureType.FUNGAL_GILLS) {
    override fun colors(parameters: FungalGillsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FungalGillsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.gillDensity,
        parameters.undulation,
        parameters.relief,
        parameters.light,
        parameters.contrast
    )
}
