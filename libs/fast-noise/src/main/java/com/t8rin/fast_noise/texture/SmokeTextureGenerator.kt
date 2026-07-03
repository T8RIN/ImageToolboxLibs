package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SmokeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val turbulence: Float = 34f,
    val density: Float = 0.56f,
    val wisps: Float = 2.2f,
    val contrast: Float = 1.35f,
    val detail: Float = 0.5f,
    val backgroundColor: Int = 0xFF111318.toInt(),
    val shadowColor: Int = 0xFF444A52.toInt(),
    val smokeColor: Int = 0xFFD5D9DD.toInt()
) : ProceduralTextureParameters

class SmokeTextureGenerator :
    NativeProceduralTextureGenerator<SmokeTextureParameters>(TextureType.SMOKE) {
    override fun colors(parameters: SmokeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.shadowColor,
        parameters.smokeColor
    )

    override fun values(parameters: SmokeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.turbulence,
        parameters.density,
        parameters.wisps,
        parameters.contrast,
        parameters.detail
    )
}
