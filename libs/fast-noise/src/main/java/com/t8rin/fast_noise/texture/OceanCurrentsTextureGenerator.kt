package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class OceanCurrentsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val currentDensity: Float = 0.70f,
    val curl: Float = 0.72f,
    val streamSharpness: Float = 0.76f,
    val glow: Float = 0.68f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF031629.toInt(),
    val primaryColor: Int = 0xFF075B8B.toInt(),
    val secondaryColor: Int = 0xFF21B9C2.toInt(),
    val highlightColor: Int = 0xFFD9FFF5.toInt()
) : ProceduralTextureParameters

class OceanCurrentsTextureGenerator :
    NativeProceduralTextureGenerator<OceanCurrentsTextureParameters>(TextureType.OCEAN_CURRENTS) {
    override fun colors(parameters: OceanCurrentsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: OceanCurrentsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.currentDensity,
        parameters.curl,
        parameters.streamSharpness,
        parameters.glow,
        parameters.contrast
    )
}
