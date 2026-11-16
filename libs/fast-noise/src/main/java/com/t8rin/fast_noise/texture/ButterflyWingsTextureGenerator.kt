package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ButterflyWingsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val veinDensity: Float = 0.55f,
    val wingShape: Float = 0.48f,
    val veinWidth: Float = 0.55f,
    val iridescence: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF1B1B27.toInt(),
    val primaryColor: Int = 0xFFBB622C.toInt(),
    val secondaryColor: Int = 0xFF477E9B.toInt(),
    val highlightColor: Int = 0xFFF3D69B.toInt()
) : ProceduralTextureParameters

class ButterflyWingsTextureGenerator :
    NativeProceduralTextureGenerator<ButterflyWingsTextureParameters>(TextureType.BUTTERFLY_WINGS) {
    override fun colors(parameters: ButterflyWingsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ButterflyWingsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.veinDensity,
        parameters.wingShape,
        parameters.veinWidth,
        parameters.iridescence,
        parameters.contrast
    )
}
