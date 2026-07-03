package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class EclipseCoronaTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val moonSize: Float = 0.23f,
    val coronaSize: Float = 0.2f,
    val rays: Float = 34f,
    val turbulence: Float = 0.62f,
    val diamondRing: Float = 0.8f,
    val spaceColor: Int = 0xFF02030A.toInt(),
    val coronaColor: Int = 0xFF765BFF.toInt(),
    val hotColor: Int = 0xFFFF9B45.toInt(),
    val lightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class EclipseCoronaTextureGenerator :
    NativeProceduralTextureGenerator<EclipseCoronaTextureParameters>(TextureType.ECLIPSE_CORONA) {
    override fun colors(parameters: EclipseCoronaTextureParameters) = intArrayOf(
        parameters.spaceColor,
        parameters.coronaColor,
        parameters.hotColor,
        parameters.lightColor
    )

    override fun values(parameters: EclipseCoronaTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.moonSize,
        parameters.coronaSize,
        parameters.rays,
        parameters.turbulence,
        parameters.diamondRing
    )
}
