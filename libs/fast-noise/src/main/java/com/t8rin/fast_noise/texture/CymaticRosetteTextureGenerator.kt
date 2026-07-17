package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CymaticRosetteTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val petalCount: Float = 0.68f,
    val resonance: Float = 0.44f,
    val ridgeSharpness: Float = 0.78f,
    val glow: Float = 0.76f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF071426.toInt(),
    val primaryColor: Int = 0xFF176FB0.toInt(),
    val secondaryColor: Int = 0xFF55D3DE.toInt(),
    val highlightColor: Int = 0xFFFFE9A5.toInt()
) : ProceduralTextureParameters

class CymaticRosetteTextureGenerator :
    NativeProceduralTextureGenerator<CymaticRosetteTextureParameters>(TextureType.CYMATIC_ROSETTE) {
    override fun colors(parameters: CymaticRosetteTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CymaticRosetteTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.petalCount,
        parameters.resonance,
        parameters.ridgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
