package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class JuliaSetTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val iterations: Float = 0.68f,
    val constantPhase: Float = 0.52f,
    val filamentSharpness: Float = 0.78f,
    val glow: Float = 0.76f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF030713.toInt(),
    val primaryColor: Int = 0xFF214E96.toInt(),
    val secondaryColor: Int = 0xFF8B46C7.toInt(),
    val highlightColor: Int = 0xFFFFE2A8.toInt()
) : ProceduralTextureParameters

class JuliaSetTextureGenerator :
    NativeProceduralTextureGenerator<JuliaSetTextureParameters>(TextureType.JULIA_SET) {
    override fun colors(parameters: JuliaSetTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: JuliaSetTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.iterations,
        parameters.constantPhase,
        parameters.filamentSharpness,
        parameters.glow,
        parameters.contrast
    )
}
