package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MyceliumTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val networkDensity: Float = 0.78f,
    val branching: Float = 0.72f,
    val fiberSharpness: Float = 0.8f,
    val glow: Float = 0.66f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF07100B.toInt(),
    val primaryColor: Int = 0xFF24452E.toInt(),
    val secondaryColor: Int = 0xFFB7C985.toInt(),
    val highlightColor: Int = 0xFFF4E8C1.toInt()
) : ProceduralTextureParameters

class MyceliumTextureGenerator :
    NativeProceduralTextureGenerator<MyceliumTextureParameters>(TextureType.MYCELIUM) {
    override fun colors(parameters: MyceliumTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: MyceliumTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.networkDensity,
        parameters.branching,
        parameters.fiberSharpness,
        parameters.glow,
        parameters.contrast
    )
}
