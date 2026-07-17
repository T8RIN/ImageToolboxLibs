package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class QuasicrystalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val symmetry: Float = 0.70f,
    val phaseWarp: Float = 0.36f,
    val ridgeSharpness: Float = 0.76f,
    val glow: Float = 0.62f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF090B18.toInt(),
    val primaryColor: Int = 0xFF2854A0.toInt(),
    val secondaryColor: Int = 0xFF27BEBB.toInt(),
    val highlightColor: Int = 0xFFFFD962.toInt()
) : ProceduralTextureParameters

class QuasicrystalTextureGenerator :
    NativeProceduralTextureGenerator<QuasicrystalTextureParameters>(TextureType.QUASICRYSTAL) {
    override fun colors(parameters: QuasicrystalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: QuasicrystalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.symmetry,
        parameters.phaseWarp,
        parameters.ridgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
