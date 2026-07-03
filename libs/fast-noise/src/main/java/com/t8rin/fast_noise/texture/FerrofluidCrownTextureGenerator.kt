package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FerrofluidCrownTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.012f,
    val spikes: Float = 19f,
    val spikeLength: Float = 0.14f,
    val bodySize: Float = 0.22f,
    val metallic: Float = 0.86f,
    val distortion: Float = 0.42f,
    val backgroundColor: Int = 0xFFE8E1D4.toInt(),
    val shadowColor: Int = 0xFF07090C.toInt(),
    val metalColor: Int = 0xFF353D43.toInt(),
    val highlightColor: Int = 0xFFD9F7FF.toInt()
) : ProceduralTextureParameters

class FerrofluidCrownTextureGenerator :
    NativeProceduralTextureGenerator<FerrofluidCrownTextureParameters>(TextureType.FERROFLUID_CROWN) {
    override fun colors(parameters: FerrofluidCrownTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.shadowColor,
        parameters.metalColor,
        parameters.highlightColor
    )

    override fun values(parameters: FerrofluidCrownTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.spikes,
        parameters.spikeLength,
        parameters.bodySize,
        parameters.metallic,
        parameters.distortion
    )
}
