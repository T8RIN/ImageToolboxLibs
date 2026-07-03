package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class AsphaltTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.02f,
    val aggregate: Float = 0.72f,
    val tar: Float = 0.44f,
    val wear: Float = 0.28f,
    val speckles: Float = 0.48f,
    val contrast: Float = 1.3f,
    val tarColor: Int = 0xFF17191A.toInt(),
    val asphaltColor: Int = 0xFF3E4242.toInt(),
    val stoneColor: Int = 0xFF898B85.toInt(),
    val dustColor: Int = 0xFFB0A995.toInt()
) : ProceduralTextureParameters

class AsphaltTextureGenerator :
    NativeProceduralTextureGenerator<AsphaltTextureParameters>(TextureType.ASPHALT) {
    override fun colors(parameters: AsphaltTextureParameters) = intArrayOf(
        parameters.tarColor,
        parameters.asphaltColor,
        parameters.stoneColor,
        parameters.dustColor
    )

    override fun values(parameters: AsphaltTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.aggregate,
        parameters.tar,
        parameters.wear,
        parameters.speckles,
        parameters.contrast
    )
}
