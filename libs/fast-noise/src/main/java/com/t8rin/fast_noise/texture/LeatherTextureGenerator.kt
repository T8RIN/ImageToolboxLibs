package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class LeatherTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.011f,
    val wrinkles: Float = 0.58f,
    val pores: Float = 0.42f,
    val grain: Float = 0.5f,
    val softness: Float = 0.38f,
    val shine: Float = 0.035f,
    val shadowColor: Int = 0xFF30160F.toInt(),
    val leatherColor: Int = 0xFF7B3D25.toInt(),
    val lightColor: Int = 0xFFB36B43.toInt(),
    val poreColor: Int = 0xFF1C0C08.toInt()
) : ProceduralTextureParameters

class LeatherTextureGenerator :
    NativeProceduralTextureGenerator<LeatherTextureParameters>(TextureType.LEATHER) {
    override fun colors(parameters: LeatherTextureParameters) = intArrayOf(
        parameters.shadowColor,
        parameters.leatherColor,
        parameters.lightColor,
        parameters.poreColor
    )

    override fun values(parameters: LeatherTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.wrinkles,
        parameters.pores,
        parameters.grain,
        parameters.softness,
        parameters.shine
    )
}
