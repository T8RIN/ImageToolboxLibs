package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BurningShipTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.0035f,
    val iterationDetail: Float = 0.72f,
    val centerOffset: Float = 0.48f,
    val boundarySharpness: Float = 0.80f,
    val glow: Float = 0.78f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF050207.toInt(),
    val primaryColor: Int = 0xFF5A1728.toInt(),
    val secondaryColor: Int = 0xFFE54522.toInt(),
    val highlightColor: Int = 0xFFFFD56A.toInt()
) : ProceduralTextureParameters

class BurningShipTextureGenerator :
    NativeProceduralTextureGenerator<BurningShipTextureParameters>(TextureType.BURNING_SHIP) {
    override fun colors(parameters: BurningShipTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BurningShipTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.iterationDetail,
        parameters.centerOffset,
        parameters.boundarySharpness,
        parameters.glow,
        parameters.contrast
    )
}
