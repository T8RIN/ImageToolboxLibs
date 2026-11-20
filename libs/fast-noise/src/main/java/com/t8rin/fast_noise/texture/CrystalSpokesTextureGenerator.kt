package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CrystalSpokesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branchCount: Float = 0.68f,
    val branching: Float = 0.52f,
    val crystalSharpness: Float = 0.8f,
    val glow: Float = 0.82f,
    val contrast: Float = 1.5f,
    val backgroundColor: Int = 0xFF071829.toInt(),
    val primaryColor: Int = 0xFF25678C.toInt(),
    val secondaryColor: Int = 0xFFA8E5F0.toInt(),
    val highlightColor: Int = 0xFFDFFFFF.toInt()
) : ProceduralTextureParameters

class CrystalSpokesTextureGenerator :
    NativeProceduralTextureGenerator<CrystalSpokesTextureParameters>(TextureType.CRYSTAL_SPOKES) {
    override fun colors(parameters: CrystalSpokesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CrystalSpokesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branchCount,
        parameters.branching,
        parameters.crystalSharpness,
        parameters.glow,
        parameters.contrast
    )
}
