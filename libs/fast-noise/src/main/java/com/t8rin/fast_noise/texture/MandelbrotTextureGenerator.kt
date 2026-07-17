package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MandelbrotTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.004f,
    val iterationDetail: Float = 0.68f,
    val centerOffset: Float = 0.42f,
    val boundarySharpness: Float = 0.78f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF02030B.toInt(),
    val primaryColor: Int = 0xFF173B72.toInt(),
    val secondaryColor: Int = 0xFF7B2EA3.toInt(),
    val highlightColor: Int = 0xFFFFC85C.toInt()
) : ProceduralTextureParameters

class MandelbrotTextureGenerator :
    NativeProceduralTextureGenerator<MandelbrotTextureParameters>(TextureType.MANDELBROT) {
    override fun colors(parameters: MandelbrotTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MandelbrotTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.iterationDetail,
        parameters.centerOffset,
        parameters.boundarySharpness,
        parameters.glow,
        parameters.contrast
    )
}
