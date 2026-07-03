package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class EventHorizonTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val diskTilt: Float = 0.72f,
    val horizonSize: Float = 0.15f,
    val diskWidth: Float = 0.065f,
    val lensing: Float = 0.82f,
    val stars: Float = 0.22f,
    val spaceColor: Int = 0xFF010107.toInt(),
    val diskColor: Int = 0xFFFF5A18.toInt(),
    val hotColor: Int = 0xFFFFE0A3.toInt(),
    val lensColor: Int = 0xFF7C48FF.toInt()
) : ProceduralTextureParameters

class EventHorizonTextureGenerator :
    NativeProceduralTextureGenerator<EventHorizonTextureParameters>(TextureType.EVENT_HORIZON) {
    override fun colors(parameters: EventHorizonTextureParameters) = intArrayOf(
        parameters.spaceColor,
        parameters.diskColor,
        parameters.hotColor,
        parameters.lensColor
    )

    override fun values(parameters: EventHorizonTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.diskTilt,
        parameters.horizonSize,
        parameters.diskWidth,
        parameters.lensing,
        parameters.stars
    )
}
