package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CloudTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.0045f,
    val coverage: Float = 0.48f,
    val softness: Float = 0.22f,
    val detail: Float = 0.62f,
    val distortion: Float = 24f,
    val density: Float = 0.92f,
    val skyColor: Int = 0xFF6FA9DC.toInt(),
    val shadowColor: Int = 0xFF9EADBC.toInt(),
    val lightColor: Int = 0xFFF8FBFF.toInt()
) : ProceduralTextureParameters

class CloudTextureGenerator :
    NativeProceduralTextureGenerator<CloudTextureParameters>(TextureType.CLOUDS) {
    override fun colors(parameters: CloudTextureParameters) = intArrayOf(
        parameters.skyColor,
        parameters.shadowColor,
        parameters.lightColor
    )

    override fun values(parameters: CloudTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.coverage,
        parameters.softness,
        parameters.detail,
        parameters.distortion,
        parameters.density
    )
}
