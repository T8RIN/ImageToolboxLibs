package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FluidVorticityTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val vortexDensity: Float = 0.72f,
    val turbulence: Float = 0.78f,
    val ridgeSharpness: Float = 0.70f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFF071326.toInt(),
    val primaryColor: Int = 0xFF1655A2.toInt(),
    val secondaryColor: Int = 0xFFE44574.toInt(),
    val highlightColor: Int = 0xFFFFD36E.toInt()
) : ProceduralTextureParameters

class FluidVorticityTextureGenerator :
    NativeProceduralTextureGenerator<FluidVorticityTextureParameters>(TextureType.FLUID_VORTICITY) {
    override fun colors(parameters: FluidVorticityTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FluidVorticityTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.vortexDensity,
        parameters.turbulence,
        parameters.ridgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
