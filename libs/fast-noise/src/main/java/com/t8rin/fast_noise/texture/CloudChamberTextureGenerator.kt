package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CloudChamberTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val trackDensity: Float = 0.68f,
    val deflection: Float = 0.58f,
    val trackSharpness: Float = 0.75f,
    val glow: Float = 0.82f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF080913.toInt(),
    val primaryColor: Int = 0xFF2D416A.toInt(),
    val secondaryColor: Int = 0xFF9BC5E7.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class CloudChamberTextureGenerator :
    NativeProceduralTextureGenerator<CloudChamberTextureParameters>(TextureType.CLOUD_CHAMBER) {
    override fun colors(parameters: CloudChamberTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CloudChamberTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.trackDensity,
        parameters.deflection,
        parameters.trackSharpness,
        parameters.glow,
        parameters.contrast
    )
}
