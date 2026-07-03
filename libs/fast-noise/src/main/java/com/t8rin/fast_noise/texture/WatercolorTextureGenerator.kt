package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class WatercolorTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val blooms: Float = 0.72f,
    val pigment: Float = 0.65f,
    val edges: Float = 0.38f,
    val paper: Float = 0.22f,
    val diffusion: Float = 0.62f,
    val paperColor: Int = 0xFFF4EBD8.toInt(),
    val pigmentColor: Int = 0xFF337FB3.toInt(),
    val secondaryColor: Int = 0xFFD85D79.toInt(),
    val edgeColor: Int = 0xFF493F76.toInt()
) : ProceduralTextureParameters

class WatercolorTextureGenerator :
    NativeProceduralTextureGenerator<WatercolorTextureParameters>(TextureType.WATERCOLOR) {
    override fun colors(parameters: WatercolorTextureParameters) = intArrayOf(
        parameters.paperColor,
        parameters.pigmentColor,
        parameters.secondaryColor,
        parameters.edgeColor
    )

    override fun values(parameters: WatercolorTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.blooms,
        parameters.pigment,
        parameters.edges,
        parameters.paper,
        parameters.diffusion
    )
}
