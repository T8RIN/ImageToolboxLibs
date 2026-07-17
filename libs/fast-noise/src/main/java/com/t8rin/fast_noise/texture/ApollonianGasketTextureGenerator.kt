package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ApollonianGasketTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val recursion: Float = 0.66f,
    val curvature: Float = 0.52f,
    val boundarySharpness: Float = 0.80f,
    val glow: Float = 0.72f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF080511.toInt(),
    val primaryColor: Int = 0xFF542778.toInt(),
    val secondaryColor: Int = 0xFFD34EAD.toInt(),
    val highlightColor: Int = 0xFFFFE486.toInt()
) : ProceduralTextureParameters

class ApollonianGasketTextureGenerator :
    NativeProceduralTextureGenerator<ApollonianGasketTextureParameters>(TextureType.APOLLONIAN_GASKET) {
    override fun colors(parameters: ApollonianGasketTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ApollonianGasketTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.recursion,
        parameters.curvature,
        parameters.boundarySharpness,
        parameters.glow,
        parameters.contrast
    )
}
