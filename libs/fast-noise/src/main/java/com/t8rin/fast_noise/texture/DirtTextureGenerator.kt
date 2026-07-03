package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class DirtTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val clumps: Float = 0.62f,
    val moisture: Float = 0.35f,
    val pebbles: Float = 0.22f,
    val roughness: Float = 0.68f,
    val variation: Float = 0.52f,
    val darkEarthColor: Int = 0xFF2A190E.toInt(),
    val earthColor: Int = 0xFF694226.toInt(),
    val dryColor: Int = 0xFFA1764D.toInt(),
    val pebbleColor: Int = 0xFFB1A28C.toInt()
) : ProceduralTextureParameters

class DirtTextureGenerator :
    NativeProceduralTextureGenerator<DirtTextureParameters>(TextureType.DIRT) {
    override fun colors(parameters: DirtTextureParameters) = intArrayOf(
        parameters.darkEarthColor,
        parameters.earthColor,
        parameters.dryColor,
        parameters.pebbleColor
    )

    override fun values(parameters: DirtTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.clumps,
        parameters.moisture,
        parameters.pebbles,
        parameters.roughness,
        parameters.variation
    )
}
