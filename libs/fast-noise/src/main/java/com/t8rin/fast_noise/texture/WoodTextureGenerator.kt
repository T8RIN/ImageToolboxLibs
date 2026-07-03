package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class WoodTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val rings: Float = 18f,
    val grain: Float = 0.32f,
    val distortion: Float = 4.5f,
    val stretch: Float = 3.2f,
    val contrast: Float = 1.15f,
    val darkColor: Int = 0xFF3A190B.toInt(),
    val lightColor: Int = 0xFFB96E32.toInt(),
    val poreColor: Int = 0xFF1D0D07.toInt()
) : ProceduralTextureParameters

class WoodTextureGenerator :
    NativeProceduralTextureGenerator<WoodTextureParameters>(TextureType.WOOD) {
    override fun colors(parameters: WoodTextureParameters) = intArrayOf(
        parameters.darkColor,
        parameters.lightColor,
        parameters.poreColor
    )

    override fun values(parameters: WoodTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.rings,
        parameters.grain,
        parameters.distortion,
        parameters.stretch,
        parameters.contrast
    )
}
