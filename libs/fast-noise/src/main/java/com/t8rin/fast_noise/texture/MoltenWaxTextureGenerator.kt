package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class MoltenWaxTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val blobs: Float = 6f,
    val softness: Float = 0.24f,
    val distortion: Float = 32f,
    val glow: Float = 0.58f,
    val contrast: Float = 1.3f,
    val backgroundColor: Int = 0xFF1C0929.toInt(),
    val firstColor: Int = 0xFFFF3D81.toInt(),
    val secondColor: Int = 0xFFFF8A24.toInt(),
    val glowColor: Int = 0xFFFFD45C.toInt()
) : ProceduralTextureParameters

class MoltenWaxTextureGenerator :
    NativeProceduralTextureGenerator<MoltenWaxTextureParameters>(TextureType.MOLTEN_WAX) {
    override fun colors(parameters: MoltenWaxTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.firstColor,
        parameters.secondColor,
        parameters.glowColor
    )

    override fun values(parameters: MoltenWaxTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.blobs,
        parameters.softness,
        parameters.distortion,
        parameters.glow,
        parameters.contrast
    )
}
