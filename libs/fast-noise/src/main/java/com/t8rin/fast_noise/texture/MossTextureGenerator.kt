package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MossTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val density: Float = 0.74f,
    val fibers: Float = 0.58f,
    val moisture: Float = 0.38f,
    val variation: Float = 0.62f,
    val clumps: Float = 0.46f,
    val soilColor: Int = 0xFF292516.toInt(),
    val darkMossColor: Int = 0xFF29451C.toInt(),
    val mossColor: Int = 0xFF648044.toInt(),
    val tipColor: Int = 0xFFB1BD69.toInt()
) : ProceduralTextureParameters

class MossTextureGenerator :
    NativeProceduralTextureGenerator<MossTextureParameters>(TextureType.MOSS) {
    override fun colors(parameters: MossTextureParameters) = intArrayOf(
        parameters.soilColor,
        parameters.darkMossColor,
        parameters.mossColor,
        parameters.tipColor
    )

    override fun values(parameters: MossTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.density,
        parameters.fibers,
        parameters.moisture,
        parameters.variation,
        parameters.clumps
    )
}
