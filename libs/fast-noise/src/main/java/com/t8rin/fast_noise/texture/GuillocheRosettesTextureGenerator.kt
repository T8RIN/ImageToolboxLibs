package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GuillocheRosettesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val rosetteDensity: Float = 0.55f,
    val petalCount: Float = 0.48f,
    val lineWidth: Float = 0.55f,
    val layers: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF101D38.toInt(),
    val primaryColor: Int = 0xFF258E9B.toInt(),
    val secondaryColor: Int = 0xFFCC739F.toInt(),
    val highlightColor: Int = 0xFFF2DAA5.toInt()
) : ProceduralTextureParameters

class GuillocheRosettesTextureGenerator :
    NativeProceduralTextureGenerator<GuillocheRosettesTextureParameters>(TextureType.GUILLOCHE_ROSETTES) {
    override fun colors(parameters: GuillocheRosettesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GuillocheRosettesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rosetteDensity,
        parameters.petalCount,
        parameters.lineWidth,
        parameters.layers,
        parameters.contrast
    )
}
