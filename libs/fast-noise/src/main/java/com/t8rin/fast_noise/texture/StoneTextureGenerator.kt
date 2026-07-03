package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class StoneTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.011f,
    val grain: Float = 0.3f,
    val veins: Float = 0.58f,
    val veinScale: Float = 0.024f,
    val distortion: Float = 18f,
    val contrast: Float = 1.12f,
    val darkColor: Int = 0xFF343536.toInt(),
    val lightColor: Int = 0xFFAAA79F.toInt(),
    val veinColor: Int = 0xFFE2DED3.toInt()
) : ProceduralTextureParameters

class StoneTextureGenerator :
    NativeProceduralTextureGenerator<StoneTextureParameters>(TextureType.STONE) {
    override fun colors(parameters: StoneTextureParameters) = intArrayOf(
        parameters.darkColor,
        parameters.lightColor,
        parameters.veinColor
    )

    override fun values(parameters: StoneTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.grain,
        parameters.veins,
        parameters.veinScale,
        parameters.distortion,
        parameters.contrast
    )
}
