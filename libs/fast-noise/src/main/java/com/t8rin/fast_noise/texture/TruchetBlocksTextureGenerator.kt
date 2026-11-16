package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TruchetBlocksTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val tileDensity: Float = 0.55f,
    val routing: Float = 0.48f,
    val bevel: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF112330.toInt(),
    val primaryColor: Int = 0xFF2F6679.toInt(),
    val secondaryColor: Int = 0xFFCA9764.toInt(),
    val highlightColor: Int = 0xFFE6D8B4.toInt()
) : ProceduralTextureParameters

class TruchetBlocksTextureGenerator :
    NativeProceduralTextureGenerator<TruchetBlocksTextureParameters>(TextureType.TRUCHET_BLOCKS) {
    override fun colors(parameters: TruchetBlocksTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TruchetBlocksTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tileDensity,
        parameters.routing,
        parameters.bevel,
        parameters.light,
        parameters.contrast
    )
}
